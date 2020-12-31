package com.elex.flutter_xmpp;

import org.jivesoftware.smack.packet.ExtensionElement;

import java.security.cert.Extension;

public class MessageExtension implements ExtensionElement {
    public static final String NAME_SPACE = "com.elex.extension";

    public static final String ELEMENT_TYPE = "messageType";

    public static final String ELEMENT_NAME = "message";

    public static final String ELEMENT_DURATION = "duration";

    //消息类型
    private String messageType;
    //语音时长
    private String duration;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }


    @Override
    public String getNamespace() {
        return NAME_SPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public CharSequence toXML() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(NAME_SPACE).append("\">");
        stringBuilder.append("<" + ELEMENT_TYPE + ">").append(messageType).append("</"+ELEMENT_TYPE+">");
        stringBuilder.append("<" + ELEMENT_DURATION + ">").append(duration).append("</"+ELEMENT_DURATION+">");
        stringBuilder.append("</"+ELEMENT_NAME+">");
        return stringBuilder.toString();
    }
}
