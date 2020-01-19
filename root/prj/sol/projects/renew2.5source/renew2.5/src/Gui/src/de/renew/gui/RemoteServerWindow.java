package de.renew.gui;

import de.renew.application.SimulatorPlugin;

import de.renew.net.NetInstance;
import de.renew.net.NetInstanceList;

import de.renew.plugin.PluginManager;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.RemotePlugin;
import de.renew.remote.RemoteServerRegistry;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.lang.ref.WeakReference;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * @version 1.0
 * @author Timo Carl {@literal <6carl@informatik.uni-hamburg.de>}
 */
public class RemoteServerWindow extends JFrame {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RemoteServerWindow.class);
    protected JComboBox _servers;
    protected JList _instancesList;
    protected JButton _openButton;
    protected JButton _disconnectButton;
    protected ConnectDialog _dialog;
    private RemoteServerController _controller;

    public RemoteServerWindow(RemoteServerController controller) {
        super("Remote Renew Servers");
        _controller = controller;
        initGui();
        _dialog = new ConnectDialog(this);
    }

    private void initGui() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Container root = getContentPane();
        root.setLayout(new GridBagLayout());
        GridBagConstraints gbc = null;

        // list of all connected Servers
        _servers = new JComboBox();
        _servers.setEditable(false);
        _servers.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _controller.doSelectServer();
                }
            });
        gbc = new GridBagConstraints(0, 0, 2, 1, 1, 0,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.BOTH,
                                     new Insets(5, 5, 0, 5), 1, 1);
        root.add(_servers, gbc);

        // connect button
        JButton connect = new JButton("Connect...");
        connect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _dialog.setVisible(true);
                }
            });
        gbc = new GridBagConstraints(0, 1, 1, 1, 0, 0,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 0, 5), 1, 1);
        root.add(connect, gbc);

        // disconnect button
        _disconnectButton = new JButton("Disconnect");
        _disconnectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _controller.doDisconnect();
                }
            });
        gbc = new GridBagConstraints(1, 1, 1, 1, 1, 0,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 0, 5), 1, 1);
        root.add(_disconnectButton, gbc);

        // list of net instances
        _instancesList = new JList();
        _instancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _instancesList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    _openButton.setEnabled(!_instancesList.isSelectionEmpty());
                }
            });
        _instancesList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        _controller.openNetInstance();
                    }
                }
            });
        _instancesList.setCellRenderer(new MyCellRenderer());
        JScrollPane scroll = new JScrollPane(_instancesList);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        gbc = new GridBagConstraints(0, 2, 2, 1, 1, 1,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.BOTH,
                                     new Insets(5, 5, 0, 5), 1, 1);
        root.add(scroll, gbc);

        // open net button
        _openButton = new JButton("Open net");
        _openButton.setEnabled(false);
        _openButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _controller.openNetInstance();
                }
            });
        gbc = new GridBagConstraints(0, 3, 1, 1, 0, 0,
                                     GridBagConstraints.NORTHWEST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 5, 5), 1, 1);
        root.add(_openButton, gbc);

        // close Button
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        gbc = new GridBagConstraints(1, 3, 1, 1, 1, 0,
                                     GridBagConstraints.NORTHEAST,
                                     GridBagConstraints.NONE,
                                     new Insets(5, 5, 5, 5), 1, 1);
        root.add(close, gbc);

        pack();
    }

    protected void updateView() throws RemoteException {
        int index = _servers.getSelectedIndex();

        Object[] servers = RemoteServerRegistry.instance().allServers();
        Vector<Object> v = new Vector<Object>(servers.length + 1);
        v.add("local nets");
        for (int i = 0; i < servers.length; i++) {
            v.add(servers[i]);
        }
        _servers.setModel(new DefaultComboBoxModel(v));
        if ((index < 0) && (v.size() > 0)) {
            index = 0;
        } else if (index >= v.size()) {
            index = v.size() - 1;
        }
        _servers.setSelectedIndex(index);

        _disconnectButton.setEnabled(servers.length != 0
                                     && _servers.getSelectedIndex() > 0);

        SimulatorPlugin simulatorPlugin = (SimulatorPlugin) PluginManager.getInstance()
                                                                         .getPluginByName("Renew Simulator");
        if (index > 0) {
            NetInstanceAccessor[] nets = RemoteServerRegistry.instance()
                                                             .allNetInstances(index
                                                                              - 1);
            Arrays.sort(nets,
                        new Comparator<NetInstanceAccessor>() {
                    public int compare(NetInstanceAccessor b1,
                                       NetInstanceAccessor b2) {
                        try {
                            int number1 = Integer.parseInt(b1.getID());
                            int number2 = Integer.parseInt(b2.getID());
                            if (number1 < number2) {
                                return -1;
                            }
                            if (number1 > number2) {
                                return 1;
                            }
                            return 0;
                        } catch (RemoteException e) {
                            logger.error("Exception while sorting nets", e);
                        }
                        return 0;
                    }
                });
            _instancesList.setListData(nets);
        } else if (index == 0 && simulatorPlugin.isSimulationActive()) {
            NetInstance[] nets = NetInstanceList.getAll();
            WeakNetInstanceWrapper[] weakNets = new WeakNetInstanceWrapper[nets.length];
            RemotePlugin remote = RemotePlugin.getInstance();
            for (int i = 0; i < nets.length; ++i) {
                weakNets[i] = new WeakNetInstanceWrapper(remote.wrapInstance(nets[i]));
            }
            Arrays.sort(weakNets,
                        new Comparator<WeakNetInstanceWrapper>() {
                    public int compare(WeakNetInstanceWrapper b1,
                                       WeakNetInstanceWrapper b2) {
                        try {
                            int number1 = Integer.parseInt(b1.getInstance()
                                                             .getID());
                            int number2 = Integer.parseInt(b2.getInstance()
                                                             .getID());
                            if (number1 < number2) {
                                return -1;
                            }
                            if (number1 > number2) {
                                return 1;
                            }
                            return 0;
                        } catch (RemoteException e) {
                            logger.error("Exception while sorting nets", e);
                        }
                        return 0;
                    }
                });
            _instancesList.setListData(weakNets);
        } else {
            _instancesList.setListData(new Object[0]);
        }
    }

    class ConnectDialog extends JDialog {
        protected JTextField _server;
        protected JTextField _name;

        public ConnectDialog(JFrame parent) {
            super(parent, "Connect to remote Renew server...", true);
            initConnectDialog();
        }

        private void initConnectDialog() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            Container root = getContentPane();
            root.setLayout(new GridBagLayout());
            GridBagConstraints gbc = null;

            JLabel l = new JLabel("Server ");
            gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                                         GridBagConstraints.NORTHEAST,
                                         GridBagConstraints.NONE,
                                         new Insets(10, 10, 0, 5), 1, 1);
            root.add(l, gbc);
            _server = new JTextField("localhost", 20);
            gbc = new GridBagConstraints(1, 0, 2, 1, 1, 0,
                                         GridBagConstraints.NORTHWEST,
                                         GridBagConstraints.BOTH,
                                         new Insets(5, 5, 0, 10), 1, 1);
            root.add(_server, gbc);

            l = new JLabel("Name ");
            gbc = new GridBagConstraints(0, 1, 1, 1, 0, 0,
                                         GridBagConstraints.NORTHEAST,
                                         GridBagConstraints.NONE,
                                         new Insets(5, 10, 0, 5), 1, 1);
            root.add(l, gbc);
            _name = new JTextField("default", 20);
            gbc = new GridBagConstraints(1, 1, 2, 1, 1, 0,
                                         GridBagConstraints.NORTHWEST,
                                         GridBagConstraints.BOTH,
                                         new Insets(5, 5, 0, 10), 1, 1);
            root.add(_name, gbc);

            // connect button
            JButton connect = new JButton("Connect");
            connect.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _controller.doConnect();
                    }
                });
            gbc = new GridBagConstraints(0, 2, 2, 1, 1, 0,
                                         GridBagConstraints.NORTHEAST,
                                         GridBagConstraints.NONE,
                                         new Insets(5, 5, 10, 5), 1, 1);
            root.add(connect, gbc);
            getRootPane().setDefaultButton(connect);

            // cancel button
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
            gbc = new GridBagConstraints(2, 2, 1, 1, 0, 0,
                                         GridBagConstraints.NORTHEAST,
                                         GridBagConstraints.NONE,
                                         new Insets(5, 5, 10, 10), 1, 1);
            root.add(cancel, gbc);
            pack();
        }
    }

    class MyCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);
            String str = null;
            if (value instanceof NetInstanceAccessor) {
                try {
                    str = ((NetInstanceAccessor) value).asString();
                    setText(str);
                } catch (RemoteException e) {
                }
            }
            return this;
        }
    }

    class WeakNetInstanceWrapper {
        WeakReference<NetInstanceAccessor> instance;
        String description;

        public WeakNetInstanceWrapper(NetInstanceAccessor net) {
            this.instance = new WeakReference<NetInstanceAccessor>(net);
            try {
                this.description = net.asString();
            } catch (RemoteException e) {
                this.description = e.toString();
            }
        }

        public NetInstanceAccessor getInstance() {
            return instance.get();
        }

        public String toString() {
            return description;
        }
    }
}