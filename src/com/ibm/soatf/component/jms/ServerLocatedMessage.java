package com.ibm.soatf.component.jms;


import javax.jms.Message;

public class ServerLocatedMessage {
    private final Message message;

    private final String jmsServerName;

    public ServerLocatedMessage(String jmsServerName, Message message) {
      this.message = message;
      this.jmsServerName = jmsServerName;
    }

    public Message getMessage() {
      return message;
    }

    public String getJmsServerName() {
      return jmsServerName;
    }

    @Override public String toString() {
      return jmsServerName + "-" + message;
    }
  }