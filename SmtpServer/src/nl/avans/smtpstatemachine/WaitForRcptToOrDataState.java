package nl.avans.smtpstatemachine;

import nl.avans.SmtpContext;

public class WaitForRcptToOrDataState implements SmtpStateInf {
    SmtpContext context;

    public WaitForRcptToOrDataState(SmtpContext context) {
        this.context = context;
        context.SendData("250 OK. Waiting for more RCPT TO or DATA");
    }

    @Override
    public void Handle(String data) {
        if (data.toUpperCase().startsWith("RCPT TO: ")) {
            context.AddRecipient(data.substring(8).trim());
            context.SendData("250 OK. Added recipient " + data.substring(8).trim());
            return;
        }
        if (data.toUpperCase().startsWith("DATA")) {
            context.SendData("354 Start mail input; end with <CRLF>.<CRLF>");
            context.SetNewState(new ReceivingDataState(context));
            return;
        }
        if (data.toUpperCase().equals("QUIT")) {
            context.SendData("221 Bye");
            context.DisconnectSocket();
            return;
        }
        context.SendData("503 Error: expecting RCPT TO or DATA");
    }
}
