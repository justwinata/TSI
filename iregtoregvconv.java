import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InputMismatchException;

public class iregtoregvconv {

	private static Scanner sc;
	private static FileWriter fileWriter;
	private static File file;
	private static int pointer;
	private static int memCount;

	public static void main (String[] args) {
		try {
			file = new File(args[0]);
			sc = new Scanner(file);
			try {
				//Get device name if available
				fileWriter = new FileWriter(args[0] + ".regv");
				String device = sc.nextLine();
				if (device.startsWith("// Device: ")) {
					fileWriter.write(device.substring(11,device.length()));
				}
				else {
					fileWriter.write("Unknown");
				}
				fileWriter.write('\n');
			fileWriter.write(String.format("r&:%02X v:%02x p:%d n:%s\n", 0x40, pointer, 0, "N/A"));	
				while (sc.hasNextLine()) {
					String in = sc.nextLine();
					//Get MemCount to know when to stop
					if (in.startsWith("MemCount:")) {
						memCount = Integer.decode(in.substring(9,in.length()));
					}

					//Parse and write to file
					if (in.startsWith("0x") && (in.length() >= 16)) {				
						pointer = Integer.decode(in.substring(0,4));
						if (pointer == memCount - 1) {
							write(in);
							break;
						}
						write(in);
					}
				}
				fileWriter.close();
			} catch(IOException ioEx) {
				System.out.println(ioEx.toString());
			}
		} catch(FileNotFoundException fnfe) {
			System.out.println(fnfe.toString());
		}
		sc.close();
	}
	private static void write(String in) {
		String hi = in.substring(10,12);
		String mid = in.substring(12,14);
		String lo = in.substring(14,16);
		try {
			//fileWriter.write(String.format("r&:%02X v:%02x p:%d n:%s\n", 0x40, pointer, 0, "N/A"));	
			fileWriter.write(String.format("r&:%02X v:"+lo+" p:%d n:%s\n", 0x3A, 0, "N/A"));	
			fileWriter.write(String.format("r&:%02X v:"+mid+" p:%d n:%s\n", 0x3B, 0, "N/A"));	
			fileWriter.write(String.format("r&:%02X v:"+hi+" p:%d n:%s\n", 0x3C, 0, "N/A"));
			fileWriter.write("WRITE\n");
		} catch(IOException ioEx) {
			System.out.println(ioEx.toString());
		}
	}
}