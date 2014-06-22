package com.alvaroga.tecuidamos;

import android.os.AsyncTask;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {

	private String user;
	private String pass;

	public Mail(String user, String pass) {
		this.user = user.trim().toLowerCase();
		this.pass = pass.trim();
	}

	public void sendMail(String email, String subject, String messageBody) {
		Session session = createSessionObject();

		try {
			Message message = createMessage(email, subject, messageBody,
					session);
			new SendMailTask().execute(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Message createMessage(String email, String subject,
			String messageBody, Session session) throws MessagingException,
			UnsupportedEncodingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("TeCuidamos@gmail.com",
				"TeCuidamos"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				email, email));
		message.setSubject(subject);
		message.setText(messageBody);
		return message;
	}

	private Session createSessionObject() {
		Properties properties = new Properties();
		//Solo es valido para Gmail, porque he cogido el smtp de Gmail.
		//En un futuro, implementar otros correos?
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");

		return Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pass);
			}
		});
	}
	//Una tarea network-related no puede ocupar la hebra principal de ejecución: Necesitamos un Async para enviar los mails.
	//Params = Message, Progress/Result null 
	private class SendMailTask extends AsyncTask<Message, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
		}

		@Override
		protected Void doInBackground(Message... messages) {
			try {
				Transport.send(messages[0]);
				System.out.println("EMAIL ENVIADO");
			} catch (Exception e) {
				//Mensaje no enviado. Fallo de autenticacion//No se ha podido ejecutar el Async.
				e.printStackTrace();
			}
			return null;
		}
	}
}