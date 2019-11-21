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

    public static int width;
    public static int height;
    public static Bitmap bm;

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

        bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
        width = bm.getWidth();
        height = bm.getHeight();

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

            int offset = 2;
            // int width = MainActivity.width / 2;
            // int hegiht = MainActivity.height / 2;

            // Muestra el logo
            //canvas.drawBitmap(MainActivity.bm, offset, offset, null);


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


            int comanda = 215;
            int separador = 30;

            for (int i = 0; i < 8; i++){
                comanda = comanda + separador;
                title.setTextSize(23);
                canvas.drawText("2", leftMargin, comanda, title);
                title.setTextSize(23);
                canvas.drawText("Coca-Cola", leftMidMargin, comanda, title);
                title.setTextSize(23);
                canvas.drawText("2,20", rightMidMargin, comanda, title);
                title.setTextSize(23);
                canvas.drawText("4,40", rightMargin, comanda, title);
            }

            separador = 24;
            comanda = comanda + separador;
            // Separacion 24
            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, comanda, title);

            separador = 30;
            comanda = comanda + separador;

            title.setTextSize(33);
            canvas.drawText("TOTAL EUROS", leftMargin, comanda, title);

            title.setTextSize(33);
            canvas.drawText("29,00", rightMargin - 20, comanda, title);

            separador = 24;
            comanda = comanda + separador;

            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, comanda, title);

            separador = 30;

            comanda = comanda + separador;

            title.setTextSize(21);
            canvas.drawText("Camarero", leftMargin, comanda, title);
            title.setTextSize(21);
            canvas.drawText("Jaime", rightMargin, comanda, title);

            comanda = comanda + separador;

            title.setTextSize(21);
            canvas.drawText("Hora Inicio", leftMargin, comanda, title);
            title.setTextSize(21);
            canvas.drawText("14:32", rightMargin, comanda, title);

            comanda = comanda + separador;

            title.setTextSize(21);
            canvas.drawText("Hora Fin", leftMargin, comanda, title);
            title.setTextSize(21);
            canvas.drawText("17:50", rightMargin, comanda, title);

            separador = 24;
            comanda = comanda + separador;

            title.setTextSize(20);
            canvas.drawText("────────────────────────────────────", leftMargin, comanda, title);

            // Extras
            separador = 30;
            comanda = comanda + separador;

            title.setTextSize(21);
            canvas.drawText("Email", leftMargin, comanda, title);
            title.setTextSize(21);
            canvas.drawText("ricondelvergeles@gmail.com", rightMargin - 205, comanda, title);

            comanda = comanda + separador;

            title.setTextSize(21);
            canvas.drawText("Telefono", leftMargin, comanda, title);
            title.setTextSize(21);
            canvas.drawText("958764567", rightMargin - 40, comanda, title);



            // Insertar la imagen

            // Letra: monospace | Opcional



        }

    }
}
