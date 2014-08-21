package de.xsrc.palaver.xmpp;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class UiUtils {
	public static StackPane getAvatar(String name) {
		int modena_colors[] = { 0xf3622d, 0xfba71b, 0xFF673ab7, 0x41a9c9,
				0x9a42c8, 0xc84164, 0xFF00bcd4, 0x888888 };
		int color = modena_colors[(int) ((name.hashCode() & 0xffffffffl) % modena_colors.length)];
		String hexcolor = String.format("#%06X", (0xFFFFFF & color));
		Rectangle r = new Rectangle(64, 64);
		r.setFill(Paint.valueOf(hexcolor));
		r.setArcHeight(5);
		r.setArcWidth(5);
		r.setStrokeType(StrokeType.INSIDE);
		r.setStroke(Paint.valueOf("BLACK"));
		StackPane sp = new StackPane();
		Text t = new Text(name.substring(0, 1).toUpperCase());
		t.setFont(Font.font(60));
		sp.getChildren().add(r);
		sp.getChildren().add(t);
		return sp;
	}
}
