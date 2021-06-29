package git.lewisbirks.pdfbox;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;

public class SlipRuleStamper {

  private static final String FILE_NAME = "dummy.pdf";
  private static final float FONT_SIZE = 12f;
  private static final String FONT_LOCATION = "fonts/Arial.ttf";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final ClassLoader LOADER = SlipRuleStamper.class.getClassLoader();

  private final InputStream inputStream;
  private final String outputFileName;

  public SlipRuleStamper(InputStream inputStream, String outputFileName) {
    this.inputStream = inputStream;
    this.outputFileName = outputFileName;
  }

  public static void main(String[] args) throws IOException {
    InputStream inputStream;
    String fileName;
    if (args.length == 1) {
      Path path = Path.of(args[0]);
      inputStream = new FileInputStream(path.toFile());
      fileName = path.getFileName().toString();
    } else {
      inputStream = LOADER.getResourceAsStream(FILE_NAME);
      fileName = FILE_NAME;
    }
    new SlipRuleStamper(inputStream, fileName).run();
  }

  void run() throws IOException {
    PDDocument doc = PDDocument.load(inputStream);
    PDFont font = PDType0Font.load(doc, LOADER.getResourceAsStream(FONT_LOCATION));
    update(doc, font, LocalDate.now());
    doc.save(new File(outputFileName));
    doc.close();
  }

  private void update(PDDocument doc, PDFont font, LocalDate date) throws IOException {
    final PDPage page = doc.getPage(0);
    final PDRectangle pageSize = page.getMediaBox();
    final String message = "Amended under the slip rule - " + date.format(FORMATTER);
    final float stringWidth = font.getStringWidth(message) * FONT_SIZE / 1000f;
    final float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * FONT_SIZE / 1000f;
    final float x = (pageSize.getWidth() - stringWidth) / 2f;
    final float y = pageSize.getHeight() - stringHeight * 2f;

    final PDPageContentStream content = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);

    // hide previous amendment message
    content.setNonStrokingColor(Color.WHITE);
    content.addRect(x, y, stringWidth, stringHeight);
    content.fill();

    // write new amendment message
    content.beginText();
    content.setNonStrokingColor(Color.RED);
    content.setFont(font, FONT_SIZE);
    content.setTextMatrix(Matrix.getTranslateInstance(x, y));
    content.showText(message);
    content.endText();

    content.close();
  }
}
