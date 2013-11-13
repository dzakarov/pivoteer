import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class Pivoter {
	Map<String, Map<String, String>> mValues = (//
	new HashMap<String, Map<String, String>>(1000000)//
	);
	Set<String> mAtts = new HashSet<String>();
	String mCfg;

	public void process() {
		// separator=;
		// input.encoding=UTF-16
		// key.zero.based.position=0
		// attribute.name.zero.based.position=1
		// attribute.value.zero.based.position=2
		CSVReader reader = null;
		CSVWriter writer = null;
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(mCfg));
			String separator = props.getProperty("separator");
			String inEncoding = props.getProperty("input.encoding");
			String outEncoding = props.getProperty("output.encoding");

			int idPos = Integer.parseInt(props
					.getProperty("key.zero.based.position"));
			int attPos = Integer.parseInt(props
					.getProperty("attribute.name.zero.based.position"));
			int valPos = Integer.parseInt(props
					.getProperty("attribute.value.zero.based.position"));
			String infile = props.getProperty("input");
			String outfile = props.getProperty("output");
			String idHeader = props.getProperty("id.header");
			boolean skipFirst = 1 == Integer.parseInt(props
					.getProperty("skip.first.line"));

			BufferedReader fileReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(infile),
							Charset.forName(inEncoding)));
			CSVParser parser = new CSVParser(separator.charAt(0), '\u0000');

			// reader = new CSVReader(fileReader, separator.charAt(0));
			String line = null;
			boolean first = true;
			int cnt = 0;
			while ((line = fileReader.readLine()) != null) {

				if (first && skipFirst) {
					first = false;
					continue;
				}
				cnt++;

				System.out.println(cnt + " lines read.");

				String[] fields = parser.parseLine(line);

				String key = fields[idPos];
				String aName = fields[attPos];
				mAtts.add(aName);
				String aValue = fields[valPos];
				Map<String, String> avMap = mValues.get(key);
				if (avMap == null) {
					avMap = new TreeMap<String, String>();
					mValues.put(key, avMap);
				}
				avMap.put(aName, aValue);
			}

			int outcnt = 0;
			OutputStreamWriter ioWriter = new OutputStreamWriter(//
					new FileOutputStream(outfile), Charset.forName(outEncoding));
			writer = new CSVWriter(ioWriter, separator.charAt(0), '\u0000');

			String[] nextLine = new String[mAtts.size() + 1];
			int i = 0;
			nextLine[i] = idHeader;
			i++;
			for (String attName : mAtts) {
				nextLine[i] = attName;
				i++;
			}

			writer.writeNext(nextLine);
			outcnt++;

			for (String id : mValues.keySet()) {
				nextLine = new String[mAtts.size() + 1];
				i = 0;
				nextLine[i] = id;
				i++;
				Map<String, String> avMap = mValues.get(id);
				for (String attName : mAtts) {
					nextLine[i] = avMap.get(attName);
					i++;
				}
				writer.writeNext(nextLine);
				outcnt++;
				System.out.println(outcnt + " lines were written.");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	public Pivoter(String cfg) {
		mCfg = cfg;
	}

	public static void main(String argv[]) {
		if (argv.length < 1) {
			System.out.println("Usage: pivoter <cfg-file-name>");
			System.out.println("See test.csv.properties for the example");
			System.exit(-1);
		}
		Pivoter pivoter = new Pivoter(argv[0]);
		pivoter.process();
	}
}
