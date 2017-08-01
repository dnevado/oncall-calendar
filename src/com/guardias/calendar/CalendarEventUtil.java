package com.guardias.calendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;



import com.google.api.services.calendar.model.AclRule.Scope;
import com.sun.xml.internal.ws.message.EmptyMessageImpl;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Calendars;
import com.google.api.services.calendar.model.Events;
import com.guardias.Util;
import com.guardias.database.ConfigurationDBImpl;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;


import com.google.api.services.calendar.model.Colors;
import com.google.api.services.calendar.model.ColorDefinition;


public class CalendarEventUtil {

	Calendar service =null;
	
	String _EMAIL_GOOGLE_ACCOUNT = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_EMAIL_OWNER(), new Long(-1)).getValue();

	
	public   void  InitCalendarService() throws IOException {
	if (service==null)
	{ 	
		service = new CalendarService().configure();	       	
	}
	}
	
	public   CalendarEventUtil() throws IOException {

	}
	
	public Event  CreateEvent(String Titulo, String Descripcion,String color,  java.util.Calendar Inicio,  java.util.Calendar Fin) throws IOException {
	
		
		System.out.println("Creating event " + Descripcion);
		Event event = new Event();
        String pageToken = null;
        if (service==null)
        {	
        	service = new CalendarService().configure();
        }
        
        event.setSummary(Titulo);
        event.setLocation("No localización");
        event.setDescription(Descripcion);
        
        event.setColorId(color);
        
        java.util.Calendar startCal = java.util.Calendar.getInstance();        
        Date startDate = Inicio.getTime();

        java.util.Calendar endCal = java.util.Calendar.getInstance();      
        Date endDate = Fin.getTime();

        //DateTime endDateTime = new DateTime("2015-05-28T17:00:00-07:00");
        DateTime start = new DateTime(startDate);        
        event.setStart(new EventDateTime().setDateTime(start));
        DateTime end = new DateTime(endDate);
        event.setEnd(new EventDateTime().setDateTime(end));
        
        System.out.println("End Creating event " + Descripcion);
        return event;
        
	}
	
	public void  SendEvent(Event ev) throws IOException {
		
		SendEvent(ev, new ArrayList<String>());
	}
	
	public  Event SendEvent(Event ev, List<String> EmailAddress) throws IOException {
		
		boolean bSendNotifications=false;
		if (EmailAddress!=null && EmailAddress.size()>0)
		{
		
			ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
	        
			for (String Email : EmailAddress)
			{
				attendees.add(new EventAttendee().setEmail(Email));
				System.out.println("Adding event to " + Email);
			}
			
	        ev.setAttendees(attendees);
	        
	        bSendNotifications = true;
        
		}
	        
        //Create access rule with associated scope
        AclRule rule = new AclRule();
        
        Scope scope = new Scope();
        scope.setType("user").setValue(_EMAIL_GOOGLE_ACCOUNT);
        rule.setScope(scope).setRole("owner");

        

        Event createdEvent =  null;
        if (bSendNotifications)
        	createdEvent =  service.events().insert("primary", ev).setSendNotifications(true).execute();
        else
        	createdEvent = service.events().insert("primary", ev).execute();
        
        AclRule createdRule = service.acl().insert("primary", rule).execute();
        
        System.out.println("Sending event" + ev.getDescription() + "," + ev.getEnd());
        
        return createdEvent;
		
	}

	public void  DeleteEventByEventId(String EventId) throws IOException {
    	
    	// Delete an event
		try
		{
			
			service.events().delete("primary", EventId).execute();
			System.out.println("Borrando Event Calendario:" + EventId);
		}
		catch (Exception e)
		{
			System.out.println("Error borrando Event Calendario:" + EventId +  ":" + e.getMessage() );
		}
    	
	}
    public void  DeleteEvent(Event ev) throws IOException {
    	
    	// Delete an event
    	service.events().delete("primary", ev.getId()).execute();
	}
	
	
	
    public Event  SetRemainder(Event ev, Long MinutesBefore) throws IOException {
    	
    	EventReminder[] reminderOverrides = new EventReminder[] {
        	    new EventReminder().setMethod("email").setMinutes(MinutesBefore.intValue()),
        	//    new EventReminder().setMethod("popup").setMinutes(10),
        	};
        Event.Reminders reminders = new Event.Reminders()
        	    .setUseDefault(false)
        	    .setOverrides(Arrays.asList(reminderOverrides));
        
        ev.setReminders(reminders);
		
        return ev;
	}
	
 

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        Event event = new Event();
        Calendar service =null;
        String pageToken = null;
        
        service = new CalendarService().configure();
        
        service.events().delete("primary","kl7ngjjv85qjf9mg7ukn5nujbc").execute();
        
             
                
        do {
          CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
          List<CalendarListEntry> items = calendarList.getItems();

          for (CalendarListEntry calendarListEntry : items) {
            System.out.println(calendarListEntry.getSummary());
          }
          pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        
        
        
        
         do {
        	  com.google.api.services.calendar.model.Events events = service.events().list("primary").setPageToken(pageToken).execute();
        	  java.util.List<Event> items = events.getItems();
        	  for (Event event1 : items) {
        	    System.out.println(event1.getSummary());
        	  }
        	  pageToken = events.getNextPageToken();
        	} while (pageToken != null);

        event.setSummary("Guardia Dia 14 Febrero Presencia");
        event.setLocation("ES");
        event.setDescription("Guardia Dia 14 Febrero Presencia");
        
        EventReminder[] reminderOverrides = new EventReminder[] {
        	    new EventReminder().setMethod("email").setMinutes(24 * 60),
        	//    new EventReminder().setMethod("popup").setMinutes(10),
        	};
        Event.Reminders reminders = new Event.Reminders()
        	    .setUseDefault(false)
        	    .setOverrides(Arrays.asList(reminderOverrides));
        
        event.setReminders(reminders);
        
        
        Colors colors  = service.colors().get().execute();
        // Print available event colors
        for (Map.Entry<String, ColorDefinition> color : colors.getEvent().entrySet()) {
          System.out.println("ColorId : " + color.getKey());
          System.out.println("  Background: " + color.getValue().getBackground());
          System.out.println("  Foreground: " + color.getValue().getForeground());
          
          event.setColorId("5");
        }
        
       
        
        

        ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
        
        attendees.add(new EventAttendee().setEmail("dnevado@gmail.com"));
        //attendees.add(new EventAttendee().setEmail("ana.maria.castano.leon@gmail.com"));
        
        // ...
        event.setAttendees(attendees);


        // set the number of days
        java.util.Calendar startCal = java.util.Calendar.getInstance();
        startCal.set(java.util.Calendar.MONTH, 1);
        startCal.set(java.util.Calendar.DATE, 16);
        startCal.set(java.util.Calendar.HOUR_OF_DAY, 20);
        startCal.set(java.util.Calendar.MINUTE, 0);
        Date startDate = startCal.getTime();

        java.util.Calendar endCal = java.util.Calendar.getInstance();
        endCal.set(java.util.Calendar.MONTH, 1);
        endCal.set(java.util.Calendar.DATE, 16);
        endCal.set(java.util.Calendar.HOUR_OF_DAY, 20);
        endCal.set(java.util.Calendar.MINUTE, 0);
        Date endDate = endCal.getTime();


        DateTime start = new DateTime(startDate);
        event.setStart(new EventDateTime().setDateTime(start));
        DateTime end = new DateTime(endDate);
        event.setEnd(new EventDateTime().setDateTime(end));

        // Create access rule with associated scope
        AclRule rule = new AclRule();
        
        Scope scope = new Scope();
        scope.setType("user").setValue("refundable.tech@gmail.com");
        rule.setScope(scope).setRole("owner");

        // Insert new access rule
        
        
        Event createdEvent = service.events().insert("primary", event).setSendNotifications(true).execute();
        
        AclRule createdRule = service.acl().insert("primary", rule).execute();

        System.out.println("Data is :"+createdEvent.getId() + "," + createdEvent.getStart()); 
    }
}  