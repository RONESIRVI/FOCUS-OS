import java.text.SimpleDateFormat
import java.util.Date

val formatter = SimpleDateFormat("dd-MMM-yyyy_HH-mm", java.util.Locale.getDefault())
val dateStr = formatter.format(Date())
println("Date is $dateStr")
