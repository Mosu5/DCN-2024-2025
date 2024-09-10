package nl.avans.smtpstatemachine;

import nl.avans.*;

public class WelcomeState implements SmtpStateInf {

    SmtpContext context;
    public WelcomeState(SmtpContext smtpContext) {
        context=smtpContext;
        context.SendData("220 smtp.example.com Welcome at this amazing smtp server!");
    }

    @Override
    public void Handle(String data) {
       if(data.toUpperCase().startsWith("HELO ")) {
            context.SetHost(data.substring(5));
            context.SendData("250 Hello " + context.GetHost() + ", I am glad to meet you");
            context.SetNewState(new WaitForMailFromState(context));
            return;
       }
        if(data.toUpperCase().startsWith("QUIT")) {
            context.SendData("221 Bye");
            context.DisconnectSocket();
            return;
        }
        context.SendData("503 Error...");
    }
}
