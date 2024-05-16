import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class LibraryTest {
    public static void main(String[] args) {
        // Test iText PDF creation
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("test.pdf"));
            document.open();
            document.add(new Paragraph("iText PDF Creation Test"));
            document.close();
            System.out.println("PDF created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test SQLite connection
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT)");
            stmt.execute("INSERT INTO test (name) VALUES ('SQLite Connection Test')");
            conn.close();
            System.out.println("SQLite connection successful.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
