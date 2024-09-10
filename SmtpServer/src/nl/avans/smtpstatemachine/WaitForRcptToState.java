package nl.avans.smtpstatemachine;

import nl.avans.SmtpContext;

public class WaitForRcptToState implements SmtpStateInf {
    SmtpContext context;

    public WaitForRcptToState(SmtpContext context) {
        this.context = context;
        context.SendData("250 OK. Waiting for RCPT TO");
    }

    @Override
    public void Handle(String data) {
        if (data.toUpperCase().startsWith("RCPT TO: ")) {
            context.AddRecipient(data.substring(8).trim());
            context.SendData("250 OK. Recipient " + data.substring(8).trim());
            context.SetNewState(new WaitForRcptToOrDataState(context));
            return;
        }
        if (data.toUpperCase().equals("QUIT")) {
            context.SendData("221 Bye");
            context.DisconnectSocket();
            return;
        }
        context.SendData("503 Error: expecting RCPT TO");
    }
}
