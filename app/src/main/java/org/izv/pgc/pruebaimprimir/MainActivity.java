package org.izv.pgc.pruebaimprimir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.fonts.Font;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
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
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button bt1, bt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        bt1 = findViewById(R.id.button);
        bt2 = findViewById(R.id.button2);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPhotoPrint();
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPrint();
            }
        });

        int offset = 50;

        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);

        Bitmap defBitmap = Bitmap.createBitmap(bm.getWidth() + offset / 2, bm.getHeight() + offset / 2, Bitmap.Config.ARGB_8888);
    }

    private void doPrint() {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = this.getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName, new MyPrintDocumentAdapter(this),
                null); //
    }

    private void doPhotoPrint() {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.captain);
        photoPrinter.printBitmap("captain.jpg - test print", bitmap);
    }

    private class MyPrintDocumentAdapter extends PrintDocumentAdapter {

        Context context;
        private int pageHeight;
        private int pageWidth;
        private PrintedPdfDocument myPdfDocument;
        private int totalPages = 1;// Habria que contar el total de paginas

        public MyPrintDocumentAdapter(Context context) {
            this.context = context;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            myPdfDocument = new PrintedPdfDocument(context, newAttributes);
            pageHeight = newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
            pageWidth = newAttributes.getMediaSize().getWidthMils() / 1000 * 72;

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            // Compute the expected number of printed pages
            //totalPages = computePageCount(newAttributes);

            if (totalPages > 0) {
                PrintDocumentInfo.Builder builder = new
                        PrintDocumentInfo
                                .Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalPages);
                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            } else {
                callback.onLayoutFailed("Page count is zero.");
            }
        }

      /*  private int computePageCount(PrintAttributes printAttributes) {
            int itemsPerPage = 4; // default item count for portrait mode

            PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
            if (!pageSize.isPortrait()) {
                // Six items per page in landscape orientation
                itemsPerPage = 6;
            }

            // Determine number of print items
            int printItemCount = getPrintItemCount();

            return (int) Math.ceil(printItemCount / itemsPerPage);
        }*/

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {

            for (int i = 0; i < totalPages; i++) {

                PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth,
                        pageHeight, i).create();
                PdfDocument.Page page = myPdfDocument.startPage(newPage);

                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    myPdfDocument.close();
                    myPdfDocument = null;
                    return;
                }

                drawPage(page);
                myPdfDocument.finishPage(page);

            }

            try {
                myPdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }
            callback.onWriteFinished(pages);

        }

        private void drawPage(PdfDocument.Page page) {
            Canvas canvas = page.getCanvas();

            // units are in points (1/72 of an inch)
            int titleBaseLine = 80;

            int leftMargin = 60;
            int leftMidMargin = 175;

            int rightMargin = 500;
            int rightMidMargin = 375;

            int separadorEstandar = 100;

            // 1081 x 321 dimensiones del logo
            Canvas cv = page.getCanvas();


            /*
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(36);
            canvas.drawText("Test Title", leftMargin, titleBaseLine, paint);


            paint.setTextSize(11);
            canvas.drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint);
            */

            Paint title = new Paint();
            title.setColor(Color.BLACK);
            title.setTextSize(50);
            canvas.drawText("Rincon del vergeles", leftMargin, titleBaseLine, title);

            title.setTextSize(33);
            canvas.drawText("Pedidos Realizados", leftMargin, separadorEstandar + 35, title);

            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, separadorEstandar + 60, title);

            title.setTextSize(23);
            canvas.drawText("Unid.", leftMargin, separadorEstandar + 91, title);
            title.setTextSize(23);
            canvas.drawText("Descripcion", leftMidMargin, separadorEstandar + 91, title);
            title.setTextSize(23);
            canvas.drawText("Precio", rightMidMargin, separadorEstandar + 91, title);
            title.setTextSize(23);
            canvas.drawText("Total", rightMargin, separadorEstandar + 91, title);

            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, separadorEstandar + 115, title);

            /*

            for (int i = 0; i < comanda.size(); i++){
                title.setTextSize(23);
                canvas.drawText(num, leftMargin, separadorEstandar + 140, title);
                title.setTextSize(23);
                canvas.drawText(producto, leftMidMargin, separadorEstandar + 140, title);
                title.setTextSize(23);
                canvas.drawText(precio, rightMidMargin, separadorEstandar + 140, title);
                title.setTextSize(23);
                canvas.drawText(precioTotal = precioTotal * num, rightMargin, separadorEstandar + 140, title);
            }

            */

            title.setTextSize(23);
            canvas.drawText("2", leftMargin, separadorEstandar + 140, title);
            title.setTextSize(23);
            canvas.drawText("Coca-Cola", leftMidMargin, separadorEstandar + 140, title);
            title.setTextSize(23);
            canvas.drawText("2,20", rightMidMargin, separadorEstandar + 140, title);
            title.setTextSize(23);
            canvas.drawText("4,40", rightMargin, separadorEstandar + 140, title);

            title.setTextSize(23);
            canvas.drawText("2", leftMargin, separadorEstandar + 170, title);
            title.setTextSize(23);
            canvas.drawText("Rosca", leftMidMargin, separadorEstandar + 170, title);
            title.setTextSize(23);
            canvas.drawText("3,80", rightMidMargin, separadorEstandar + 170, title);
            title.setTextSize(23);
            canvas.drawText("7,60", rightMargin, separadorEstandar + 170, title);

            title.setTextSize(23);
            canvas.drawText("4", leftMargin, separadorEstandar + 195, title);
            title.setTextSize(23);
            canvas.drawText("Cerveza", leftMidMargin, separadorEstandar + 195, title);
            title.setTextSize(23);
            canvas.drawText("3", rightMidMargin, separadorEstandar + 195, title);
            title.setTextSize(23);
            canvas.drawText("12", rightMargin, separadorEstandar + 195, title);

            title.setTextSize(23);
            canvas.drawText("1", leftMargin, separadorEstandar + 220, title);
            title.setTextSize(23);
            canvas.drawText("Postre", leftMidMargin, separadorEstandar + 220, title);
            title.setTextSize(23);
            canvas.drawText("5", rightMidMargin, separadorEstandar + 220, title);
            title.setTextSize(23);
            canvas.drawText("5", rightMargin, separadorEstandar + 220, title);

            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, separadorEstandar + 244, title);

            title.setTextSize(33);
            canvas.drawText("TOTAL EUROS", leftMargin, separadorEstandar + 274, title);

            title.setTextSize(33);
            canvas.drawText("29,00", rightMargin - 20, separadorEstandar + 274, title);

            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, separadorEstandar + 304, title);

            title.setTextSize(21);
            canvas.drawText("Email", leftMargin, separadorEstandar + 334, title);
            title.setTextSize(21);
            canvas.drawText("Telefono", rightMargin, separadorEstandar + 334, title);


            //title.setTextSize();


            // Insertar la imagen

            // Letra: monospace

            // Dibujar
            // paint.setColor(Color.BLUE);
            // canvas.drawRect(leftMargin, 140, 172, 212, paint);


        }
    }
}
