import com.solacesystems.jcsmp.*;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class Application {
    public static void main(String args[]) throws JCSMPException {
        System.out.println("Test ok");

            final JCSMPProperties properties = new JCSMPProperties();
            properties.setProperty(JCSMPProperties.HOST, "localhost");
            properties.setProperty(JCSMPProperties.VPN_NAME, "default");
            properties.setProperty(JCSMPProperties.USERNAME, "sampleUser");

            final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
            session.connect();

            final CountDownLatch latch = new CountDownLatch(1);
            final XMLMessageConsumer messageConsumer = session.getMessageConsumer(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage){
                        System.out.printf("TextMessage Received '%s'%n ",((TextMessage) bytesXMLMessage).getText());
                        System.out.println(Long.toString(new Date().getTime() - Long.parseLong(((TextMessage) bytesXMLMessage).getText())));
                    }else{
                        System.out.println("Message Received ::" + bytesXMLMessage.toString());
                    }
                    System.out.printf("Message Dump %n%s%n",bytesXMLMessage.dump());
                    latch.countDown();
                }

                @Override
                public void onException(JCSMPException e) {
                    System.out.printf("Consumer received execption: %s%n",e);
                    latch.countDown();
                }
            });

            final Topic topic = JCSMPFactory.onlyInstance().createTopic("tutorial/topic");
            session.addSubscription(topic);
            messageConsumer.start();

            try{
                synchronized (latch){
                    latch.wait();
                }

            }catch (InterruptedException e){
                System.out.println("Latch was broken while waiting");
            }

        messageConsumer.close();
        System.out.println("Exiting.");
        session.closeSession();
    }
}
