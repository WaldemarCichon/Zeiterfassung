package de.cisoft.utility.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

public class FtpCouldNotConnectException extends IOException {
	private static final long serialVersionUID = 87123141L;

	public FtpCouldNotConnectException(FTPClient ftp) {
		super("Could not connect to ftp server with address:"+ftp.getRemoteAddress().getHostAddress()+" with replay "+ftp.getReplyString());
	}
}
