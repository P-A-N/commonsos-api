package commonsos.service.image;


import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import commonsos.Configuration;

@ExtendWith(MockitoExtension.class)
public class QrCodeServiceTest {

  @Spy Configuration config;
  @Spy Random random;
  @InjectMocks private QrCodeService qrCodeService;

  @Test
  public void getTransactionQrCode() throws Exception {
    File file = qrCodeService.getTransactionQrCode("hoge==", 2L);
    
    QRCodeReader reader = new QRCodeReader();
    BufferedImage image = ImageIO.read(file);
    LuminanceSource source = new BufferedImageLuminanceSource(image);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    
    String content = reader.decode(bitmap).getText();
    file.delete();
    
    assertThat(content).isEqualTo("https://" + config.commonsosHost() + config.downloadPagePath() + "?userId=hoge%3D%3D&communityId=2");
  }
  
  @Test
  public void getTransactionQrCode_amount1() throws Exception {
    File file = qrCodeService.getTransactionQrCode("hoge==", 2L, new BigDecimal("1"));
    
    QRCodeReader reader = new QRCodeReader();
    BufferedImage image = ImageIO.read(file);
    LuminanceSource source = new BufferedImageLuminanceSource(image);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    
    String content = reader.decode(bitmap).getText();
    file.delete();
    
    assertThat(content).isEqualTo("https://" + config.commonsosHost() + config.downloadPagePath() + "?userId=hoge%3D%3D&communityId=2&amount=1");
  }
  
  @Test
  public void getTransactionQrCode_amount1_5() throws Exception {
    File file = qrCodeService.getTransactionQrCode("hoge==", 2L, new BigDecimal("1.5"));
    
    QRCodeReader reader = new QRCodeReader();
    BufferedImage image = ImageIO.read(file);
    LuminanceSource source = new BufferedImageLuminanceSource(image);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    
    String content = reader.decode(bitmap).getText();
    file.delete();
    
    assertThat(content).isEqualTo("https://" + config.commonsosHost() + config.downloadPagePath() + "?userId=hoge%3D%3D&communityId=2&amount=1.5");
  }
  
  @Test
  public void getTransactionQrCode_amount0_00001() throws Exception {
    File file = qrCodeService.getTransactionQrCode("hoge==", 2L, new BigDecimal("0.00001"));
    
    QRCodeReader reader = new QRCodeReader();
    BufferedImage image = ImageIO.read(file);
    LuminanceSource source = new BufferedImageLuminanceSource(image);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    
    String content = reader.decode(bitmap).getText();
    file.delete();
    
    assertThat(content).isEqualTo("https://" + config.commonsosHost() + config.downloadPagePath() + "?userId=hoge%3D%3D&communityId=2&amount=0.00001");
  }
}