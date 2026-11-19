package app.arifnurrohim.linkding;

import android.content.Context;
import android.net.Uri;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AccountBackup {
    public static final String ROOT = "linkding-backup";
    private static final int MAX_BYTES = 2 * 1024 * 1024;

    private AccountBackup() {
    }

    public static String defaultFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_'at'_HH-mm-ss", Locale.US);
        return "LinkdingBackup_" + format.format(new Date()) + ".xml";
    }

    public static void write(Context context, Uri uri, List<Prefs.Account> accounts) throws Exception {
        String xml = toXml(accounts);
        OutputStream out = context.getContentResolver().openOutputStream(uri);
        if (out == null) throw new IOException("Could not open the backup file");
        try {
            out.write(xml.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } finally {
            out.close();
        }
    }

    public static String toXml(List<Prefs.Account> accounts) throws Exception {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializer.startTag(null, ROOT);
        serializer.attribute(null, "version", "1");
        for (Prefs.Account account : accounts) {
            serializer.startTag(null, "account");
            serializer.attribute(null, "name", account.name);
            serializer.attribute(null, "server", account.server);
            serializer.attribute(null, "token", account.token);
            serializer.attribute(null, "trust_all", String.valueOf(account.trustAll));
            serializer.endTag(null, "account");
        }
        serializer.endTag(null, ROOT);
        serializer.endDocument();
        return writer.toString();
    }

    public static List<Prefs.Account> read(Context context, Uri uri) throws Exception {
        InputStream in = context.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Could not open the backup file");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] chunk = new byte[8192];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
                if (buffer.size() > MAX_BYTES) {
                    throw new IOException("The backup file is too large");
                }
            }
        } finally {
            in.close();
        }
        return fromXml(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
    }

    public static List<Prefs.Account> fromXml(String xml) throws Exception {
        List<Prefs.Account> accounts = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));

        boolean sawRoot = false;
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if (!sawRoot) {
                    if (!ROOT.equals(tag)) {
                        throw new IllegalArgumentException("Not a LinkDing backup file");
                    }
                    sawRoot = true;
                } else if ("account".equals(tag)) {
                    String server = Prefs.normalizeServer(
                            value(parser.getAttributeValue(null, "server")));
                    String token = value(parser.getAttributeValue(null, "token"));
                    if (server.isEmpty() || token.isEmpty()) continue;
                    String name = value(parser.getAttributeValue(null, "name"));
                    boolean trustAll = Boolean.parseBoolean(
                            parser.getAttributeValue(null, "trust_all"));
                    accounts.add(new Prefs.Account(name, server, token, trustAll));
                }
            }
            event = parser.next();
        }
        if (!sawRoot) throw new IllegalArgumentException("Not a LinkDing backup file");
        return accounts;
    }

    private static String value(String text) {
        return text == null ? "" : text.trim();
    }
}
