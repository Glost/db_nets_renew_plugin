package de.renew.navigator;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.renew.navigator.models.Directory;
import de.renew.navigator.models.DirectoryType;
import de.renew.navigator.models.Leaf;
import de.renew.navigator.models.Model;
import de.renew.navigator.models.SearchFilter;
import de.renew.navigator.models.TreeElement;

import de.renew.plugin.PluginManager;

import java.io.File;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-08-25
 */
public class AutosaveController extends NavigatorController {
    private final DocumentBuilder documentBuilder;
    private final Transformer transformer;
    private final File autosaveFile;
    private SwingWorker<Document, Object> worker;

    /**
     * Log4j logger instance.
     */
    public static final Logger logger = Logger.getLogger(AutosaveController.class);

    /**
     * @param plugin the plugin containing the controller
     */
    public AutosaveController(NavigatorPlugin plugin) {
        super(plugin);

        // Load the autosave file
        autosaveFile = new File(PluginManager.getPreferencesLocation(),
                                "navigator.xml");

        try {
            // Init document builder
            documentBuilder = DocumentBuilderFactory.newInstance()
                                                    .newDocumentBuilder();

            // Init transformer
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                          "4");
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onModelChanged(Object target) {
        // Cancel the current worker if it is not done yet.
        if (worker != null && !worker.isDone()) {
            logger.trace("Cancelled old worker.");
            worker.cancel(true);
        }

        // Restart the worker.
        worker = new SwingWorker<Document, Object>() {
                @Override
                protected Document doInBackground() throws Exception {
                    return createXmlDocument();
                }

                @Override
                protected void done() {
                    if (isCancelled()) {
                        return;
                    }

                    try {
                        DOMSource source = new DOMSource(get());
                        StreamResult result = new StreamResult(autosaveFile);
                        try {
                            transformer.transform(source, result);
                        } catch (TransformerException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException ignored) {
                    } catch (ExecutionException e) {
                        logger.error("Could not create autosave XML file", e);
                    }
                }
            };

        worker.execute();
    }

    /**
     * Saves the model to XML.
     */
    public void loadModel() {
        model.clear();
        try {
            Document doc = documentBuilder.parse(autosaveFile);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();

            // Load text filter
            model.setTextSearch(null);
            final NodeList textSearchElements = rootElement.getElementsByTagName("text-search");
            if (textSearchElements.getLength() > 0) {
                Element textSearchElement = (Element) textSearchElements.item(0);

                String name = textSearchElement.getAttribute("name");
                SearchFilter.Type type = SearchFilter.Type.valueOf(textSearchElement
                                                                   .getAttribute("type"));
                boolean caseSensitive = Boolean.valueOf(textSearchElement
                                            .getAttribute("case-sensitive"));
                final NodeList termElements = textSearchElement
                                                  .getElementsByTagName("term");
                String[] terms = new String[termElements.getLength()];
                for (int j = 0; j < termElements.getLength(); ++j) {
                    Element termElement = (Element) termElements.item(j);
                    terms[j] = termElement.getTextContent();
                }

                SearchFilter textSearch = new SearchFilter(name, type,
                                                           caseSensitive, terms);

                model.setTextSearch(textSearch);
            }

            // Load filters
            Element fileFiltersElement = (Element) rootElement.getElementsByTagName("filters")
                                                              .item(0);
            for (int i = 0; i < fileFiltersElement.getChildNodes().getLength();
                         ++i) {
                final Node node = fileFiltersElement.getChildNodes().item(i);

                if (!(node instanceof Element)) {
                    continue;
                }

                Element fileFilterElement = (Element) node;

                String name = fileFilterElement.getAttribute("name");
                SearchFilter.Type type = SearchFilter.Type.valueOf(fileFilterElement
                                                                   .getAttribute("type"));
                boolean caseSensitive = Boolean.valueOf(fileFilterElement
                                            .getAttribute("case-sensitive"));
                final NodeList termElements = fileFilterElement
                                                  .getElementsByTagName("term");
                String[] terms = new String[termElements.getLength()];
                for (int j = 0; j < termElements.getLength(); ++j) {
                    Element termElement = (Element) termElements.item(j);
                    terms[j] = termElement.getTextContent();
                }

                SearchFilter fileFilter = new SearchFilter(name, type,
                                                           caseSensitive, terms);
                model.activateFileFilter(fileFilter);
            }

            // Load trees
            Element treesElement = (Element) rootElement.getElementsByTagName("trees")
                                                        .item(0);
            for (int i = 0; i < treesElement.getChildNodes().getLength();
                         ++i) {
                final Node node = treesElement.getChildNodes().item(i);

                if (!(node instanceof Element)) {
                    continue;
                }

                Element treeElement = (Element) node;
                loadTreeElementFromXml(model, treeElement);
            }

            model.notifyObservers(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true, when the autosave file exists.
     */
    public boolean isAutosaveFileExisting() {
        return autosaveFile.exists();
    }

    /**
     * Creates the XML document from the current model state.
     */
    private Document createXmlDocument() {
        // Create the document
        Document doc = documentBuilder.newDocument();
        Element rootElement = doc.createElement("navigator");
        doc.appendChild(rootElement);

        // Save search filter
        if (model.getTextSearch() != null) {
            Element textSearchElement = doc.createElement("text-search");
            rootElement.appendChild(textSearchElement);

            saveSearchFilterToElement(model.getTextSearch(), textSearchElement);
        }

        // Save filters
        Element fileFiltersElement = doc.createElement("filters");
        rootElement.appendChild(fileFiltersElement);
        for (SearchFilter fileFilter : model.getActiveFileFilters()) {
            Element fileFilterElement = doc.createElement("filter");
            fileFiltersElement.appendChild(fileFilterElement);

            saveSearchFilterToElement(fileFilter, fileFilterElement);
        }

        // Save trees
        Element xmlTrees = doc.createElement("trees");
        rootElement.appendChild(xmlTrees);
        for (TreeElement element : model.getTreeRoots()) {
            saveTreeElementToXml(element, xmlTrees);
        }
        return doc;
    }

    /**
     * @param filter the filter to save.
     * @param element the targeted element.
     */
    private void saveSearchFilterToElement(SearchFilter filter, Element element) {
        element.setAttribute("name", filter.getName());
        element.setAttribute("case-sensitive",
                             String.valueOf(filter.isCaseSensitive()));
        element.setAttribute("type", filter.getType().toString());
        for (String term : filter.getTerms()) {
            Element termsElement = element.getOwnerDocument()
                                          .createElement("term");
            element.appendChild(termsElement);
            termsElement.setTextContent(term);
        }
    }

    /**
     * Saves a tree element to a XML node.
     *
     * @param treeElement element to save
     * @param xml target XML node
     */
    private void saveTreeElementToXml(TreeElement treeElement, Element xml) {
        // Create and append child node
        final String elementName = treeElementToName(treeElement);
        final Element childXml = xml.getOwnerDocument()
                                    .createElement(elementName);
        xml.appendChild(childXml);

        // Set attributes
        childXml.setAttribute("name", treeElement.getName());
        childXml.setAttribute("excluded",
                              String.valueOf(treeElement.isExcluded()));
        childXml.setAttribute("file", treeElement.getFile().toString());

        // Set further values
        if (treeElement instanceof Directory) {
            Directory directory = (Directory) treeElement;
            childXml.setAttribute("opened", String.valueOf(directory.isOpened()));
            if (directory.getType() != null) {
                childXml.setAttribute("type", directory.getType().toString());
            }

            for (TreeElement childElement : directory.getChildren()) {
                saveTreeElementToXml(childElement, childXml);
            }
        }
    }

    /**
     * Loads a tree element from a XML node.
     *
     * @param parent element to add the given value to
     * @param xml source XML node
     */
    private void loadTreeElementFromXml(Model parent, Element xml) {
        // Create element and add to parent.
        final TreeElement treeElement = nameToTreeElement(xml.getTagName());
        parent.add(treeElement);

        // Set generic attributes.
        treeElement.setName(xml.getAttribute("name"));
        treeElement.setExcluded(Boolean.valueOf(xml.getAttribute("excluded")));
        treeElement.setFile(new File(xml.getAttribute("file")));

        // Set further values
        if (treeElement instanceof Directory) {
            final Directory directory = (Directory) treeElement;

            // Set directory attributes.
            directory.setOpened(Boolean.valueOf(xml.getAttribute("opened")));
            if (xml.hasAttribute("type")) {
                directory.setType(DirectoryType.valueOf(xml.getAttribute("type")));
            } else {
                directory.setType(null);
            }

            // Load children.
            for (int i = 0; i < xml.getChildNodes().getLength(); ++i) {
                final Node node = xml.getChildNodes().item(i);

                if (node instanceof Element) {
                    // Create and append child node
                    loadTreeElementFromXml(directory, (Element) node);
                }
            }
        }
    }

    /**
     * @param elementName XML element name
     * @return tree element
     */
    private TreeElement nameToTreeElement(String elementName) {
        if (elementName.equals("file")) {
            return new Leaf();
        }

        return new Directory();
    }

    /**
     * @param treeElement tree element to find name for
     * @return XML element name
     */
    private String treeElementToName(TreeElement treeElement) {
        final Class<?extends TreeElement> treeElementClass = treeElement
                                                                 .getClass();

        if (treeElementClass.equals(Leaf.class)) {
            return "file";
        }

        return "directory";
    }
}