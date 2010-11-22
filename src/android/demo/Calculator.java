package android.demo;

import android.app.Activity;
import android.content.Intent;
import android.demo.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Calculator extends Activity {
	Button one, two, three, four, five, six, seven, eight, nine, zero,add,
			multiply, divide, dot, plus, minus, widget86, clear, equal,close;
	TextView screen;
	double mul = 0, mul1 = 0, mul2;
	
	String[] doubleStrs = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calculator);

		one = (Button) this.findViewById(R.id.one);

		widget86 = (Button) this.findViewById(R.id.widget86);
		widget86.setEnabled(false);
		two = (Button) this.findViewById(R.id.two);
		close = (Button) this.findViewById(R.id.close);
		three = (Button) this.findViewById(R.id.three);
		four = (Button) this.findViewById(R.id.four);
		five = (Button) this.findViewById(R.id.five);
		six = (Button) this.findViewById(R.id.six);
		seven = (Button) this.findViewById(R.id.seven);
		eight = (Button) this.findViewById(R.id.eight);
		nine = (Button) this.findViewById(R.id.nine);
		zero = (Button) this.findViewById(R.id.zero);
		clear = (Button) this.findViewById(R.id.clear);
		dot = (Button) this.findViewById(R.id.dot);
		multiply = (Button) this.findViewById(R.id.multiply);
		//add = (Button) this.findViewById(R.id.add);
		screen = (TextView) this.findViewById(R.id.screen);
		equal = (Button)this.findViewById(R.id.equals);
		
		
		if (close.isClickable()) {
			close.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					finish();

				}
			});
		}

		hookupButton();
	}

	private void hookupButton() {
		if (equal.isClickable())
			equal.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					doubleStrs = screen.getText().toString().split("+");
					double product = 1.0;
					
					for (String doubleStr : doubleStrs) {
						product += Double.parseDouble(doubleStr);
					}
					screen.setText(Double.toString(product));
				}
			});
		
		if (clear.isClickable())
			clear.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText("");
				}
			});

		if (one.isClickable()) {
			one.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "1");
				}
			});
		}
		if (two.isClickable()) {
			two.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "2");
				}
			});
		}
		if (three.isClickable()) {
			three.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "3");
				}
			});
		}
		if (four.isClickable()) {
			four.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "4");
				}
			});
		}
		if (five.isClickable()) {
			five.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "5");
				}
			});
		}
		if (six.isClickable()) {
			six.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "6");
				}
			});
		}
		if (seven.isClickable()) {
			seven.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "7");
				}
			});
		}
		if (eight.isClickable()) {
			eight.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "8");
				}
			});
		}
		if (nine.isClickable()) {
			nine.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "9");
				}
			});
		}
		if (zero.isClickable()) {
			zero.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + "0");
				}
			});
		}
		if (dot.isClickable()) {
			dot.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					screen.setText(screen.getText() + ".");
				}
			});
		}
		if (multiply.isClickable()) {
			multiply.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					// screen.setText(screen.getText()+".");
					screen.setText(screen.getText() + "+");
					// mul2=mul*mul1;
					// screen.setText(Double.toString(mul2));
				}
			});
		}

	}

}
