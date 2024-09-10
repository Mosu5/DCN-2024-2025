package nl.avans.smtpstatemachine;

import nl.avans.SmtpContext;

public class ReceivingDataState implements SmtpStateInf {
    SmtpContext context;

    public ReceivingDataState(SmtpContext context) {
        this.context = context;
    }

    @Override
    public void Handle(String data) {
        if (data.equals(".")) {
            context.SendData("250 OK. Message accepted.");
            context.SetNewState(new WaitForMailFromState(context));
            return;
        }
        context.AddTextToBody(data);
    }
}
