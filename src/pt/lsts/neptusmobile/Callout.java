package pt.lsts.neptusmobile;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import pt.lsts.neptusmobile.data.DataFragment;
import pt.lsts.neptusmobile.data.ImcSystem;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class Callout {
	protected static final String TAG = "Callout";
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private DataFragment dataFrag;
	// CAllout
	private TextToSpeech ttp;
	private final boolean calloutOn = false;
	private ScheduledFuture calloutHandle;

	public void calloutPlay(final String selectedSys) {
		final Runnable callout = new Runnable() {
			@Override
			public void run() { 
				Log.i(TAG, "Start callout");
				ttp.setLanguage(Locale.UK);
				ImcSystem sys = dataFrag.getSystem(selectedSys);
				// while (true) {
					Log.i(TAG, "Adding " + sys.getSpeed());
					ttp.speak(number2Text(sys.getSpeed()),
							TextToSpeech.QUEUE_FLUSH, null);
				// }

				// while (calloutOn) {
				// Log.i(TAG, "Adding " + sys.getSpeed());
				// ttp.speak(number2Text(sys.getSpeed()),
				// TextToSpeech.QUEUE_FLUSH, null);
				// // Thread.sleep(5000);
				// }
			}

		};
		calloutHandle = scheduler.scheduleAtFixedRate(callout, 0, 5,
				TimeUnit.SECONDS);
		// scheduler.schedule(new Runnable() {
		// @Override
		// public void run() {
		// calloutHandle.cancel(true);
		// }
		// }, 60 * 60, TimeUnit.SECONDS);
	};

	public void stopCallouts (){
		calloutHandle.cancel(true);
	}

	private String number2Text(Float number) {
		String numberS = number.toString();
		String[] tokens = numberS.split("\\.");
		Log.i(TAG, numberS + " has " + tokens.length + " tokens");
		StringBuilder text = new StringBuilder();
		char tens, unit, decimal;
		// Assing values
		switch (tokens[0].length()) {
			case 1:
				tens = 'N';
				unit = tokens[0].charAt(0);
				break;
			case 2:
				tens = tokens[0].charAt(0);
				unit = tokens[1].charAt(1);
				break;
			default:
				return "";
		}
		switch (tokens.length) {
			case 1:
				decimal = 'N';
				break;
			case 2:
				decimal = tokens[1].charAt(0);
				break;

			default:
				return "";
		}

		Log.i(TAG, "Tens: " + tens);
		Log.i(TAG, "Units: " + unit);
		Log.i(TAG, "Decimal: " + decimal);
		// To text
		switch (tens) {
			case 'N':
				// Append nothing
				break;
			case '0':
				text.append(getString(R.string._0));
				text.append(' ');
				break;
			case '2':
				text.append(getString(R.string._20));
				text.append(' ');
				break;
			case '3':
				text.append(getString(R.string._30));
				text.append(' ');
				break;
			case '4':
				text.append(getString(R.string._40));
				text.append(' ');
				break;
			default:
				Log.i(TAG, "Defaulting, Tens: " + tens);
				return getString(R.string.tooFast);
		}
		if (tens != '1') {
			appendUnits(text, unit);
		} else {
			appendFrom10To19(text, unit);
		}
		if (decimal != 'N') {
			text.append(getString(R.string.dot));
			text.append(' ');
			appendUnits(text, decimal);
		}
		Log.i(TAG, "Result: " + text.toString());
		return text.toString();
	}

	private void appendUnits(StringBuilder text, char unit) {
		switch (unit) {
			case '0':
				text.append(getString(R.string._0));
				text.append(' ');
				break;
			case '1':
				text.append(getString(R.string._1));
				text.append(' ');
				break;
			case '2':
				text.append(getString(R.string._2));
				text.append(' ');
				break;
			case '3':
				text.append(getString(R.string._3));
				text.append(' ');
				break;
			case '4':
				text.append(getString(R.string._4));
				text.append(' ');
				break;
			case '5':
				text.append(getString(R.string._5));
				text.append(' ');
				break;
			case '6':
				text.append(getString(R.string._6));
				text.append(' ');
				break;
			case '7':
				text.append(getString(R.string._7));
				text.append(' ');
				break;
			case '8':
				text.append(getString(R.string._8));
				text.append(' ');
				break;
			case '9':
				text.append(getString(R.string._9));
				text.append(' ');
				break;
		}
	}

	private void appendFrom10To19(StringBuilder text, char unit) {
		switch (unit) {
			case '0':
				text.append(getString(R.string._10));
				text.append(' ');
				break;
			case '1':
				text.append(getString(R.string._11));
				text.append(' ');
				break;
			case '2':
				text.append(getString(R.string._12));
				text.append(' ');
				break;
			case '3':
				text.append(getString(R.string._13));
				text.append(' ');
				break;
			case '4':
				text.append(getString(R.string._14));
				text.append(' ');
				break;
			case '5':
				text.append(getString(R.string._15));
				text.append(' ');
				break;
			case '6':
				text.append(getString(R.string._16));
				text.append(' ');
				break;
			case '7':
				text.append(getString(R.string._17));
				text.append(' ');
				break;
			case '8':
				text.append(getString(R.string._18));
				text.append(' ');
				break;
			case '9':
				text.append(getString(R.string._19));
				text.append(' ');
				break;
		}
	}
}