package org.game.throne.proxy.forward.codec;

import java.util.Arrays;

/**
 * Created by lvtu on 2017/9/14.
 */
public enum CommandEnum {
    HTTP(1), NEW(2), CLOSE(3), REUSE(4), PIPELINE(5), CLOSE_PIPELINE(6), ACK_PIPELINE(7);

    private int commandCode;

    CommandEnum(int commandCode) {
        this.commandCode = commandCode;
    }


    public int getCommandCode() {
        return commandCode;
    }

    public static CommandEnum of(int commandCode) {
        return Arrays.asList(values()).stream().filter(command -> command.commandCode == commandCode).findFirst().get();
    }
}
