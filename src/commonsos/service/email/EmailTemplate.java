package commonsos.service.email;

public enum EmailTemplate {
  CREATE_ACCOUNT("アカウント作成のご確認", "CREATE_ACCOUNT.vm"),
  CREATE_ADMIN("アカウント作成のご確認", "CREATE_ADMIN.vm"),
  UPDATE_USER_EMAIL("メールアドレス変更のご確認", "UPDATE_USER_EMAIL.vm"),
  UPDATE_ADMIN_EMAIL("メールアドレス変更のご確認", "UPDATE_ADMIN_EMAIL.vm"),
  PASSWORD_RESET("パスワード再設定", "PASSWORD_RESET.vm");
  
  private String subject;
  private String filename;
  
  private EmailTemplate(String subject, String filename) {
    this.subject = subject;
    this.filename = filename;
  }
  
  public String getSubject() {
    return this.subject;
  }
  
  public String getFilename() {
    return this.filename;
  }
}
