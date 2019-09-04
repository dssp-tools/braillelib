package dssp.brailleLib;

import java.util.Calendar;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter
{
	public LogFormatter()
	{
	}

	@Override
	public String format(LogRecord r)
	{
		long m = r.getMillis();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(m);

		StringBuffer buf = new StringBuffer();
		buf.append(String.format("%4d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH)));
		buf.append(String.format(" %02d:%02d:%02d.%03d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND)));
		buf.append(String.format(" %s\n", r.getMessage()));

		return buf.toString();
	}
}
