package com.borntogeek.gmail_reader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import jakarta.mail.*;
import jakarta.mail.UIDFolder.FetchProfileItem;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.SearchTerm;

import org.apache.poi.ss.usermodel.*;

public class AdvancedGmailReader {

	// ❗ Use environment variables in real projects
	private static final String EMAIL = "your email";
	private static final String APP_PASSWORD = "your app password";

	public static void main(String[] args) throws IOException {

		List<Email> emails = readUnreadEmailsFromGmail();

		// Sort latest first
		emails.sort(
				Comparator.comparing(
						Email::getReceivedOn,
						Comparator.nullsLast(Comparator.reverseOrder())
				)
		);

		for (Email email : emails) {
			Path emailDir = saveEmailBodyToFile(email);
			saveEmailAttachments(email.getAttachments(), emailDir);
			System.out.println(email);
		}
	}

	// =========================
	// READ ONLY UNREAD EMAILS
	// =========================
	private static List<Email> readUnreadEmailsFromGmail() {

		Store store = null;
		Folder folder = null;

		try {
			store = getImapStore();
			folder = getFolderFromStore(store, "INBOX");

			Message[] messages = folder.search(getUnreadSearchTerm());
			folder.fetch(messages, getFetchProfile());

			List<Email> emails = new ArrayList<>(messages.length);

			for (Message message : messages) {
				Email email = getEmailFromMessage(message);
				emails.add(email);

				// Mark email as READ
				message.setFlag(Flags.Flag.SEEN, true);
			}

			return emails;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeFolder(folder);
			closeStore(store);
		}

		return Collections.emptyList();
	}

	// =========================
	// IMAP CONNECTION
	// =========================
	private static Store getImapStore() throws Exception {
		Session session = Session.getInstance(getImapProperties());
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", EMAIL, APP_PASSWORD);
		return store;
	}

	private static Properties getImapProperties() {
		Properties props = new Properties();
		props.put("mail.imaps.host", "imap.gmail.com");
		props.put("mail.imaps.port", "993");
		props.put("mail.imaps.ssl.trust", "imap.gmail.com");
		return props;
	}

	private static Folder getFolderFromStore(Store store, String folderName)
			throws MessagingException {
		Folder folder = store.getFolder(folderName);
		folder.open(Folder.READ_WRITE);
		return folder;
	}

	// =========================
	// SEARCH: UNREAD
	// =========================
	private static SearchTerm getUnreadSearchTerm() {
		return new FlagTerm(new Flags(Flags.Flag.SEEN), false);
	}

	private static FetchProfile getFetchProfile() {
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfileItem.ENVELOPE);
		fp.add(FetchProfileItem.CONTENT_INFO);
		return fp;
	}

	// =========================
	// EMAIL PARSING
	// =========================
	private static Email getEmailFromMessage(Message message)
			throws MessagingException, IOException {

		Email email = new Email();

		Address[] from = message.getFrom();
		email.setFrom(from != null
				? ((InternetAddress) from[0]).getAddress()
				: null);

		email.setSubject(message.getSubject());
		email.setReceivedOn(message.getReceivedDate());

		Object content = message.getContent();

		if (content instanceof String) {
			email.getTextBuilder().append(content);
		} else if (content instanceof Multipart) {
			processMultipart((Multipart) content, email);
		}

		return email;
	}

	private static void processMultipart(Multipart multipart, Email email)
			throws MessagingException, IOException {

		for (int i = 0; i < multipart.getCount(); i++) {
			BodyPart part = multipart.getBodyPart(i);

			if (part.isMimeType("text/plain")) {
				email.getTextBuilder().append(part.getContent());
			} else if (part.isMimeType("multipart/*")) {
				processMultipart((Multipart) part.getContent(), email);
			} else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
					|| part.getFileName() != null) {
				readAttachment(part, email);
			}
		}
	}

	// =========================
	// ATTACHMENTS + EXCEL READ
	// =========================
	private static void readAttachment(BodyPart part, Email email)
			throws MessagingException, IOException {

		try (InputStream is = part.getInputStream();
			 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[4096];
			int n;
			while ((n = is.read(buffer)) != -1) {
				baos.write(buffer, 0, n);
			}

			FileByteData file =
					new FileByteData(part.getFileName(), baos.toByteArray());

			email.getAttachments().add(file);

			// Excel detection
			String name = file.getFilename().toLowerCase();
			if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
				System.out.println("\n📊 Excel detected: " + file.getFilename());
				printExcelData(file);
			}
		}
	}

	// =========================
	// EXCEL PARSER (CORRECT)
	// =========================
	private static void printExcelData(FileByteData file) throws IOException {

		try (InputStream is = new ByteArrayInputStream(file.getData());
			 Workbook workbook = WorkbookFactory.create(is)) {

			for (Sheet sheet : workbook) {
				System.out.println("📄 Sheet: " + sheet.getSheetName());

				for (Row row : sheet) {
					for (Cell cell : row) {
						System.out.print(getCellValue(cell) + "\t");
					}
					System.out.println();
				}
			}
		}
	}

	private static String getCellValue(Cell cell) {
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC:
				return DateUtil.isCellDateFormatted(cell)
						? cell.getDateCellValue().toString()
						: String.valueOf(cell.getNumericCellValue());
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				return cell.getCellFormula();
			default:
				return "";
		}
	}

	// =========================
	// SAVE BODY + ATTACHMENTS
	// =========================
	private static void saveEmailAttachments(
			List<FileByteData> attachments, Path emailDir) throws IOException {

		for (FileByteData file : attachments) {
			Files.write(emailDir.resolve(file.getFilename()), file.getData());
		}
	}

	private static Path saveEmailBodyToFile(Email email) throws IOException {

		String safeSubject = email.getSubject() == null
				? "no_subject"
				: email.getSubject().replaceAll("[^a-zA-Z0-9]", "_");

		Path emailDir = Paths.get(
				"target", "emails",
				safeSubject + "_" + email.getReceivedOn().getTime()
		);

		Files.createDirectories(emailDir);

		Files.write(
				emailDir.resolve("body.txt"),
				email.getTextBuilder().toString()
						.getBytes(StandardCharsets.UTF_8)
		);

		return emailDir;
	}

	// =========================
	// CLEANUP
	// =========================
	private static void closeFolder(Folder folder) {
		try {
			if (folder != null && folder.isOpen()) {
				folder.close(true);
			}
		} catch (MessagingException ignored) {}
	}

	private static void closeStore(Store store) {
		try {
			if (store != null && store.isConnected()) {
				store.close();
			}
		} catch (MessagingException ignored) {}
	}
}
