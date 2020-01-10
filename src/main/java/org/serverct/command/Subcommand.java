package org.serverct.command;

public interface Subcommand {
    boolean execute(long qq, String[] args);
}
