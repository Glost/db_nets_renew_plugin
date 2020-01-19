# Diagram Plugin (AIP)

## Known properties of the diagram plugin:

de.renew.diagram.init	: (boolean) if true the palette is loaded on startup,
		        : if the de.renew.gui.autostart is not set to false


# Inscriptions (Messages, Actions):

## Messages:

- Messages can start with action -- complete message inscription

<pre>
  AIP:                                              Net: (NC-out inscription)
  ------------------------------------------>       |\
   action p2 = SomeObject.toAclMessage(aid)         | \
                                                    |  \
                                                    |   \
                                                    |____\
                                                    action p2 = SomeObject.toAclMessage(aid)
</pre> 
 
- Messages can have arbitrary inscriptions -- interpreted as content

<pre>
  AIP:                                              Net: (NC-out inscription)
  ------------------------------------------>       |\
              slContent                             | \
                                                    |  \
                                                    |   \
                                                    |____\
                                                    action p2 = Sl0Creator.createActionRequest(
                                                              aid,
                                                              slContent)
</pre>

- Without inscriptions the standard will be inserted in NC out

<pre>
  AIP:                                              Net: (NC-out inscription)
  ------------------------------------------>       |\
                                                    | \
                                                    |  \
                                                    |   \
                                                    |____\
                                                    action p2 = Sl0Creator.createActionRequest(
                                                             aid,
                                                             "content")
</pre>


# Actions:

- Action inscriptions will be added to the sequence as raw text.
   ":access(kb)" is already available.

<pre>
 AIP:                                             Net: (NC-sequence inscription)

       |      |                                    
  -----------------------                         |-----------------------|
  | kb:ask("name",name) |                         |                       |
  -----------------------                         |-----------------------|
       |      |                                        :access(kb)
                                                     kb:ask("name",name)
</pre>
                                                   
                                                   
                                                   
                                                   
## DC exchange:

- DC inscriptions will be added to the NC-exchange description

<pre>
 AIP:                                             Net: (NC-exchange description)
                                                  (dcNewExchange(.,.,.) channel)    
       |      |                                           /  \
  --------------------                                  /      \
  | RoleHelper.TEST  |                               RoleHelper.TEST                    
  --------------------                              /              \
       |      |                                    ------------------                               
</pre>
 
- DC inscriptions leading with the keyword "simple" will be added to the NC-simple-exchange description

<pre>
 AIP:                                             Net: (NC-simple exchange description)
                                                  (dcExchange(.,.) channel)    
       |      |                                           /  \
  ---------------------------                           /      \
  | simple RoleHelper.TEST  |                       RoleHelper.TEST                    
  ---------------------------                       /              \
       |      |                                    ------------------                               
</pre>
                                                      
 
                                                   
