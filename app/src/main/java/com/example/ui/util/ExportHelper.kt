package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.GameMatch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun exportAsPlainText(context: Context, match: GameMatch) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(match.timestamp))
        val durationStr = formatDuration(match.durationSeconds)

        val sb = StringBuilder()
        sb.append("🇮🇹 SCORE ITALY - CARD GAME SCOREBOARD 🇮🇹\n")
        sb.append("========================================\n")
        sb.append("Match Date      : $dateStr\n")
        sb.append("Target Score    : ${match.targetScore} points\n")
        sb.append("Match Duration  : $durationStr\n")
        sb.append("Final Score     : ${match.teamAName} (${match.teamAScore}) vs ${match.teamBName} (${match.teamBScore})\n")
        sb.append("Winner          : 🏆 ${match.winnerName} 🏆\n")
        sb.append("Status          : Match Completed Offline\n")
        sb.append("========================================\n\n")

        sb.append("ROUND HISTORY LOG:\n")
        sb.append("----------------------------------------\n")
        if (match.rounds.isEmpty()) {
            sb.append("No rounds were recorded.\n")
        } else {
            var teamACurrent = 0
            var teamBCurrent = 0
            for (round in match.rounds) {
                if (round.teamName == match.teamAName) {
                    teamACurrent += round.scoreChange
                } else {
                    teamBCurrent += round.scoreChange
                }
                val changeSign = if (round.scoreChange >= 0) "+${round.scoreChange}" else "${round.scoreChange}"
                sb.append("Round ${round.roundNumber}: ${round.teamName} $changeSign ")
                sb.append("(${match.teamAName}: $teamACurrent, ${match.teamBName}: $teamBCurrent)\n")
            }
        }
        sb.append("========================================\n")
        sb.append("Generated with Score Italy Scoreboard app\n")

        val rawText = sb.toString()

        try {
            // Write to a local temporary file for sharing
            val file = File(context.cacheDir, "ScoreItaly_Match_${match.id}.txt")
            FileOutputStream(file).use { out ->
                out.write(rawText.toByteArray())
            }

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Score Italy Match Result - ${match.winnerName} Won")
                putExtra(Intent.EXTRA_TEXT, rawText)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Score Italy Match Result")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportAsPdf(context: Context, match: GameMatch) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(match.timestamp))
        val durationStr = formatDuration(match.durationSeconds)

        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 size width
        val pageHeight = 842 // A4 size height
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // Styles
        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 20f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }

        val textBoldPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textRegularPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val headerCellPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val thickLinePaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        // Italian Tricolore Paints
        val paintGreen = Paint().apply { color = android.graphics.Color.parseColor("#008C45") }
        val paintWhite = Paint().apply { color = android.graphics.Color.parseColor("#EAF2F8") }
        val paintRed = Paint().apply { color = android.graphics.Color.parseColor("#CD212A") }

        // Layout variables
        val xMargin = 45f
        var yPos = 40f

        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas

        // Header drawing function for each page
        fun drawPageHeader(canvas: Canvas, pageNum: Int) {
            // Draw Italian Tricolore Accent Banner at the ultimate top edge
            val colorWidth = pageWidth / 3f
            canvas.drawRect(0f, 0f, colorWidth, 10f, paintGreen)
            canvas.drawRect(colorWidth, 0f, colorWidth * 2, 10f, paintWhite)
            canvas.drawRect(colorWidth * 2, 0f, pageWidth.toFloat(), 10f, paintRed)

            yPos = 40f
            canvas.drawText("SCORE ITALY - MATCH SCORE REPORT", xMargin, yPos, titlePaint)
            yPos += 15f
            canvas.drawText("Generated Offline on Card Match Tracker", xMargin, yPos, subtitlePaint)
            yPos += 12f
            canvas.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, thickLinePaint)
            yPos += 20f
        }

        // Output first page header
        drawPageHeader(canvas, 1)

        // Draw Summary Box
        val boxHeight = 110f
        val cardPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F4F6F7")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#BDC3C7")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        
        canvas.drawRect(xMargin, yPos, pageWidth - xMargin, yPos + boxHeight, cardPaint)
        canvas.drawRect(xMargin, yPos, pageWidth - xMargin, yPos + boxHeight, cardBorderPaint)

        val boxYInner = yPos + 20f
        canvas.drawText("MATCH INFORMATION", xMargin + 15f, boxYInner, textBoldPaint)
        canvas.drawText("Date: $dateStr", xMargin + 15f, boxYInner + 20f, textRegularPaint)
        canvas.drawText("Target Score: ${match.targetScore} pts", xMargin + 15f, boxYInner + 40f, textRegularPaint)
        canvas.drawText("Match Duration: $durationStr", xMargin + 15f, boxYInner + 60f, textRegularPaint)

        canvas.drawText("FINAL SCOREBOARD", xMargin + 270f, boxYInner, textBoldPaint)
        canvas.drawText("${match.teamAName}: ${match.teamAScore} pts", xMargin + 270f, boxYInner + 20f, textRegularPaint)
        canvas.drawText("${match.teamBName}: ${match.teamBScore} pts", xMargin + 270f, boxYInner + 40f, textRegularPaint)
        canvas.drawText("Winner: 🏆 ${match.winnerName} 🏆", xMargin + 270f, boxYInner + 60f, textBoldPaint)

        yPos += boxHeight + 35f

        // Draw Table Header
        canvas.drawText("HISTORICAL ROUND LOG", xMargin, yPos, textBoldPaint)
        yPos += 10f
        canvas.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, thickLinePaint)
        yPos += 20f

        // Headers
        canvas.drawText("Round #", xMargin + 15f, yPos, headerCellPaint)
        canvas.drawText("Team Scored", xMargin + 110f, yPos, headerCellPaint)
        canvas.drawText("Score Added", xMargin + 240f, yPos, headerCellPaint)
        canvas.drawText("${match.teamAName} Total", xMargin + 350f, yPos, headerCellPaint)
        canvas.drawText("${match.teamBName} Total", xMargin + 460f, yPos, headerCellPaint)

        yPos += 8f
        canvas.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, linePaint)
        yPos += 20f

        var teamACurrent = 0
        var teamBCurrent = 0
        var pageCount = 1

        for (round in match.rounds) {
            // Check for page overflow
            if (yPos > pageHeight - 60f) {
                // End current page
                pdfDocument.finishPage(currentPage)

                // Start new page
                pageCount++
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                drawPageHeader(canvas, pageCount)

                // Draw sub headers
                canvas.drawText("Round #", xMargin + 15f, yPos, headerCellPaint)
                canvas.drawText("Team Scored", xMargin + 110f, yPos, headerCellPaint)
                canvas.drawText("Score Added", xMargin + 240f, yPos, headerCellPaint)
                canvas.drawText("${match.teamAName} Total", xMargin + 350f, yPos, headerCellPaint)
                canvas.drawText("${match.teamBName} Total", xMargin + 460f, yPos, headerCellPaint)

                yPos += 8f
                canvas.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, linePaint)
                yPos += 20f
            }

            if (round.teamName == match.teamAName) {
                teamACurrent += round.scoreChange
            } else {
                teamBCurrent += round.scoreChange
            }

            val changeSign = if (round.scoreChange >= 0) "+${round.scoreChange}" else "${round.scoreChange}"

            canvas.drawText("${round.roundNumber}", xMargin + 15f, yPos, textRegularPaint)
            canvas.drawText(round.teamName, xMargin + 110f, yPos, textRegularPaint)
            canvas.drawText(changeSign, xMargin + 240f, yPos, textRegularPaint)
            canvas.drawText("$teamACurrent", xMargin + 350f, yPos, textRegularPaint)
            canvas.drawText("$teamBCurrent", xMargin + 460f, yPos, textRegularPaint)

            yPos += 10f
            canvas.drawLine(xMargin, yPos, pageWidth - xMargin, yPos, linePaint)
            yPos += 18f
        }

        // Draw footer text
        canvas.drawText("Score Italy Scorecard App - Play Store Quality Score Tracker", xMargin, pageHeight - 25f, subtitlePaint)
        canvas.drawText("Page $pageCount", pageWidth - xMargin - 50f, pageHeight - 25f, subtitlePaint)

        pdfDocument.finishPage(currentPage)

        try {
            val file = File(context.cacheDir, "ScoreItaly_Match_${match.id}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "Score Italy Match Result - ${match.teamAName} vs ${match.teamBName}")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Match PDF Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatDuration(durationSeconds: Long): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }
}
