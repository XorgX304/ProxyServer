package org.game.throne.proxy.forward.codec;

/**
 * Created by lvtu on 2017/9/14.
 */
public class CommandHeader {

    private CommandEnum command;

    private int pipePort;

    public final static CommandEnum HTTP_COMMAND = CommandEnum.HTTP;

    private CommandHeader(int pipePort) {
        command = CommandEnum.HTTP;
        this.pipePort = pipePort;
    }

    public CommandHeader(CommandEnum command, int pipePort) {
        this.command = command;
        this.pipePort = pipePort;
    }

    public CommandEnum getCommand() {
        return command;
    }

    public void setCommand(CommandEnum command) {
        this.command = command;
    }

    public int getPipePort() {
        return pipePort;
    }

    public void setPipePort(int pipePort) {
        this.pipePort = pipePort;
    }


}
