package commonsos.service.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
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

  public File getTransactionQrCode(String cryptoUserId, Long communityId) {
    String encodedCryptoUserId;
    try {
      encodedCryptoUserId = URLEncoder.encode(cryptoUserId, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new ServerErrorException(e);
    }
    
    String url = String.format("%s?userId=%s&communityId=%d", getDownloadPathUrl(), encodedCryptoUserId, communityId);
    int size = Integer.parseInt(config.transactionQrCodeSize());
    return getQrCode(url, size);
  }
  
  public File getTransactionQrCode(String cryptoUserId, Long communityId, BigDecimal amount) {
    String encodedCryptoUserId;
    try {
      encodedCryptoUserId = URLEncoder.encode(cryptoUserId, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new ServerErrorException(e);
    }
    
    String url = String.format("%s?userId=%s&communityId=%d&amount=%s", getDownloadPathUrl(), encodedCryptoUserId, communityId, amount.stripTrailingZeros().toString());
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
    try (InputStream logoIns = this.getClass().getResourceAsStream(config.transactionQrCodeLogoFile())) {
      BufferedImage logoImage = ImageIO.read(logoIns);

      BitMatrix bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
      MatrixToImageConfig imageConfig = new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);
      BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, imageConfig);

      int deltaHeight = qrImage.getHeight() - logoImage.getHeight();
      int deltaWidth = qrImage.getWidth() - logoImage.getWidth();

      BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = (Graphics2D) combined.getGraphics();
      g.drawImage(qrImage, 0, 0, null);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
      g.drawImage(logoImage, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

      imageFile = getTmpFile();
      ImageIO.write(combined, "png", imageFile);
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
