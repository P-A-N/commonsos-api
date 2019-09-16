package commonsos.controller.app;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import commonsos.ApiVersion;
import commonsos.controller.AbstractController;
import commonsos.view.CommonView;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

@Slf4j
public class GetAppApiVersionController extends AbstractController {

  @Override
  public CommonView handle(Request request, Response response) {
    DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
    log.info("fileItemFactory encoding=" + fileItemFactory.getDefaultCharset());
    fileItemFactory.setDefaultCharset("UTF-8");
    log.info("fileItemFactory encoding=" + fileItemFactory.getDefaultCharset());
    ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
    log.info("fileUpload encoding=" + fileUpload.getHeaderEncoding());
    fileUpload.setHeaderEncoding("UTF-8");
    log.info("fileUpload encoding=" + fileUpload.getHeaderEncoding());
    return new CommonView().setApiVersion(ApiVersion.APP_API_VERSION.toString());
  }
}
