package commonsos.service.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import commonsos.Configuration;
import commonsos.exception.ServerErrorException;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class QrCodeService {
  
  private static final String ENCODING = "UTF-8";
  
  @Inject private Configuration config;
  @Inject private Random random;

  public File getTransactionQrCode(Long userId) {
    String url = String.format("%s?userId=%d", getDownloadPathUrl(), userId);
    int size = Integer.parseInt(config.transactionQrCodeSize());
    return getQrCode(url, size);
  }
  
  public File getTransactionQrCode(Long userId, BigDecimal amount) {
    String url = String.format("%s?userId=%d&amount=%s", getDownloadPathUrl(), userId, amount.stripTrailingZeros().toString());
    int size = Integer.parseInt(config.transactionQrCodeSize());
    return getQrCode(url, size);
  }
  
  private File getQrCode(String contents, int size) {
    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    hints.put(EncodeHintType.CHARACTER_SET, ENCODING);
    hints.put(EncodeHintType.MARGIN, 0);
    
    QRCodeWriter writer = new QRCodeWriter();
    File imageFile = null;
    try {
      BitMatrix bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
      BufferedImage bImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
      imageFile = getTmpFile();
      ImageIO.write(bImage, "png", imageFile);
    } catch (Exception e) {
      if (imageFile != null) imageFile.delete();
      log.info(String.format("fail to create qr code. [contents=%s]", contents), e);
      throw new ServerErrorException(e);
    }
    
    return imageFile;
  }
  
  private String getDownloadPathUrl() {
    String host = config.commonsosHost();
    String path = config.downloadPagePath();
    return String.format("https://%s%s", host, path);
  }
  
  private File getTmpFile() {
    String tmpDirPath = System.getProperty("java.io.tmpdir");
    if (!tmpDirPath.endsWith(File.separator)) tmpDirPath = tmpDirPath + File.separator;
    
    String filePath = String.format("%s%s_%s_%d.png",
        tmpDirPath,
        "commonsos_qr",
        Thread.currentThread().getName(),
        random.nextInt(Integer.MAX_VALUE));
    
    File file = new File(filePath);
    if (file.exists()) throw new ServerErrorException(String.format("fail to create tmpFile. tmpFile already exists [%s]", filePath));
    
    return file;
  }
}
