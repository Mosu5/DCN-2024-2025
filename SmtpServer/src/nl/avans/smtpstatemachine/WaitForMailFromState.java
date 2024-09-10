package nl.avans.smtpstatemachine;

import nl.avans.SmtpContext;

public class WaitForMailFromState implements SmtpStateInf {
    SmtpContext context;

    public WaitForMailFromState(SmtpContext context) {
        this.context=context;
        context.SendData("250 OK. Waiting for MAIL FROM");
    }

    @Override
    public void Handle(String data) {
        if (data.toUpperCase().startsWith("MAIL FROM: ")) {
            context.SetMailFrom(data.substring(10).trim());
            context.SendData("250 OK. Sender " + context.GetMailFrom());
            context.SetNewState(new WaitForRcptToState(context));
            return;
        }
        if (data.toUpperCase().equals("QUIT")) {
            context.SendData("221 Bye");
            context.DisconnectSocket();
            return;
        }
        context.SendData("503 Error: expecting MAIL FROM");
    }
}
