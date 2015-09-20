package com.agileapps.pt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/*import org.apache.poi.xwpf.usermodel.XWPFDocument;
 import org.apache.poi.xwpf.usermodel.XWPFParagraph;
 import org.apache.poi.xwpf.usermodel.XWPFRun;*/



import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.graphics.pdf.PdfDocument.PageInfo.Builder;

import com.agileapps.pt.MainActivity;
import com.agileapps.pt.pojos.FormTemplate;

public class PDFWriter {

	public static PrintDocumentAdapter getPrinterAdapter(Activity activity,
			FormTemplate formTemplate) {
		return new PTPrintDocumentAdapter(activity, formTemplate);
	}

	private static class PTPrintDocumentAdapter extends PrintDocumentAdapter {
		private static final float TITLE_TEXT_SIZE = 18;
		private static final float QUESTION_TEXT_SIZE = 14;
		private static final int SPLIT_QUESTION = 65;
		private static final int PAGE_HEIGHT = 670;
		int startingVerticalPosition = 60;
		int startingLeftMargin = 54;
		private Activity activity;
		private int pageHeight;
		private int pageWidth;
		private FormTemplate formTemplate;
		public PrintedPdfDocument ptPdfDocument;
		public int totalpages = 1;

		private PTPrintDocumentAdapter(Activity activity,
				FormTemplate formTemplate) {
			this.activity = activity;
			this.formTemplate = formTemplate;
		}

		@Override
		public void onLayout(PrintAttributes oldAttributes,
				PrintAttributes newAttributes,
				CancellationSignal cancellationSignal,
				LayoutResultCallback callback, Bundle extras) {
			ptPdfDocument = new PrintedPdfDocument(
					activity.getApplicationContext(), newAttributes);
			pageHeight = newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
			pageWidth = newAttributes.getMediaSize().getWidthMils() / 1000 * 72;
			if (cancellationSignal.isCanceled()) {
				callback.onLayoutCancelled();
				return;
			}
			if (totalpages > 0) {
				PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder(
						"print_output.pdf").setContentType(
						PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(
						totalpages);

				PrintDocumentInfo info = builder.build();
				callback.onLayoutFinished(info, true);
			} else {
				callback.onLayoutFailed("Page count is zero.");
			}
		}

		@Override
		public void onWrite(PageRange[] pageRanges,
				ParcelFileDescriptor destination,
				CancellationSignal cancellationSignal,
				WriteResultCallback callback) {
			drawPages();
			try {
				ptPdfDocument.writeTo(new FileOutputStream(destination
						.getFileDescriptor()));
			} catch (IOException e) {
				callback.onWriteFailed(e.toString());
				return;
			} finally {
				ptPdfDocument.close();
				ptPdfDocument = null;
			}

			callback.onWriteFinished(pageRanges);
		}

		private void drawPages( ) {
			int pageNumber=1;
			PageInfo newPage = new PageInfo.Builder(pageWidth,
					pageHeight, pageNumber).create();
			PdfDocument.Page page = ptPdfDocument.startPage(newPage);
			Canvas canvas = page.getCanvas();
			pageNumber++; // Make sure page numbers start at 1
			int longestQuestion = 10;
			XY xy = new XY(startingLeftMargin, startingVerticalPosition);
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			String printableForm = formTemplate.getPrintableString();
			String lines[] = StringUtils.splitByWholeSeparator(printableForm,
					FormTemplate.LINE_DELIMITER);
			for (String line : lines) {
				if (!line.contains(FormTemplate.TITLE_DELIMITER)) {
                    int indexOf=line.indexOf(FormTemplate.QUESTION_DELIMITER)+1;
                    if ( indexOf > longestQuestion){
                    	longestQuestion=indexOf;
                    }
				}
			}
			if ( longestQuestion > SPLIT_QUESTION){
				longestQuestion=SPLIT_QUESTION;
			}
			boolean firstTitle=true;
			for (String line : lines) {
				if (line.contains(FormTemplate.TITLE_DELIMITER)) {
					if ( ! firstTitle){
						xy=new XY(xy.x,xy.y+10);
					}
					xy = printTitle(canvas, paint, xy, StringUtils.chomp(line,
							FormTemplate.TITLE_DELIMITER));
					firstTitle=false;
				} else if (line.contains(FormTemplate.QUESTION_DELIMITER)) {
					String questionAnswer[] = StringUtils
							.splitByWholeSeparator(line,
									FormTemplate.QUESTION_DELIMITER);
					if (questionAnswer.length > 1) {
						xy = printQuestion(canvas, paint, xy, questionAnswer[0],longestQuestion);
						xy = printAnswer(canvas, paint, xy, questionAnswer[1]);
					} else {
						xy = printQuestion(canvas, paint, xy, questionAnswer[0],longestQuestion);
					}
				}
				Log.i(MainActivity.PRINTER_INFO,"line "+line+" xy info "+xy);
				if ( xy.y > PAGE_HEIGHT ){
					xy = new XY(startingLeftMargin, startingVerticalPosition);
					ptPdfDocument.finishPage(page);
					newPage = new PageInfo.Builder(pageWidth,
							pageHeight, pageNumber).create();
					 page = ptPdfDocument.startPage(newPage);
				}
			}
			ptPdfDocument.finishPage(page);
		}

		private XY printTitle(Canvas canvas, Paint paint, XY xy, String title) {
			title = title.trim();
			paint.setTextSize(TITLE_TEXT_SIZE);
			canvas.drawText(title, xy.x, xy.y, paint);
			return new XY(xy.x, xy.y + 35);
		}

		private XY printQuestion(Canvas canvas, Paint paint, XY xy,
				String question, int longestQuestion) {
			paint.setTextSize(QUESTION_TEXT_SIZE);
			question = question.trim();
			canvas.drawText(question, xy.x, xy.y, paint);
			return new XY(xy.x + 10 + (longestQuestion * 6), xy.y);
		}

		private XY printAnswer(Canvas canvas, Paint paint, XY xy, String answer) {
			answer = answer.trim();
			paint.setTextSize(QUESTION_TEXT_SIZE);
			canvas.drawText(answer, xy.x, xy.y, paint);
			return new XY(startingLeftMargin, xy.y + 20);
		}

		private boolean pageInRange(PageRange[] pageRanges, int page) {
			for (int i = 0; i < pageRanges.length; i++) {
				if ((page >= pageRanges[i].getStart())
						&& (page <= pageRanges[i].getEnd()))
					return true;
			}
			return false;
		}

	}

	private static class XY {
		private final int x;
		private final int y;

		private XY(int x, int y) {
			this.y = y;
			this.x = x;
		}

		@Override
		public String toString() {
			return "XY [x=" + x + ", y=" + y + "]";
		}
		
		
	}

}
