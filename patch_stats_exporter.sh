cat << 'INNER_EOF' >> app/src/main/java/com/example/util/StatsExporter.kt

    fun exportViewToImage(context: Context, view: android.view.View) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.parseColor("#121212")) // Default background
        }
        view.draw(canvas)
        saveBitmapToGallery(context, bitmap)
    }
INNER_EOF
