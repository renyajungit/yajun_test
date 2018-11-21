package com.wis.basis.common.utils.security;

/**
 * @author lucky
 * 
 */
public class PasswordUtils {
	private final static byte[] alphabet = { (byte) 'a', (byte) 'b',
			(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
			(byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l',
			(byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q',
			(byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
			(byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) 'A',
			(byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
			(byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K',
			(byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P',
			(byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
			(byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
			(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
			(byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9' };

	public static String createPassword(int i) {
		byte[] pwd = new byte[i];
		for (int index = 0; index < i; index++) {
			pwd[index] = alphabet[(int) (Math.random() * 62)];
		}
		return new String(pwd);
	}

	public static String createPassword() {
		return createPassword(8);
	}

}
