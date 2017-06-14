package com.guardias.messaging;

import javax.jms.JMSException;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;


public class TextMessageSender {
    public static void main(String args[]) throws JMSException {
    	
    	
        ExampleConfiguration config = ExampleConfiguration.parseConfig("TextMessageSender", args);
        
        ExampleCommon.setupLogging();
        
        // Create the connection factory based on the config       
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withRegion(config.getRegion().getName())
                        .withCredentials(config.getCredentialsProvider())
                );
        
        // Create the connection
        SQSConnection connection = connectionFactory.createConnection();
        
        // Create the queue if needed
        ExampleCommon.ensureQueueExists(connection, config.getQueueName());
            
        // Create the session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer( session.createQueue( config.getQueueName() ) );
        
        sendMessages(session, producer);
 
        // Close the connection. This closes the session automatically
        connection.close();
        System.out.println( "Connection closed" );
    }
 
    private static void sendMessages( Session session, MessageProducer producer ) {
        BufferedReader inputReader = new BufferedReader(
            new InputStreamReader( System.in, Charset.defaultCharset() ) );
        
        try {
            String input;
            while( true ) { 
                System.out.print( "Enter message to send (leave empty to exit): " );
                input = inputReader.readLine();
                if( input == null || input.equals("" ) ) break;
                
                TextMessage message = session.createTextMessage(input);
                producer.send(message);
                System.out.println( "Send message " + message.getJMSMessageID() );
            }
        } catch (EOFException e) {
            // Just return on EOF
        } catch (IOException e) {
            System.err.println( "Failed reading input: " + e.getMessage() );
        } catch (JMSException e) {
            System.err.println( "Failed sending message: " + e.getMessage() );
            e.printStackTrace();
        }
    }
}