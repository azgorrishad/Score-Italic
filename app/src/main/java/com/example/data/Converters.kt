package com.example.data

import androidx.room.TypeConverter
import com.example.data.model.Round
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromRoundList(rounds: List<Round>?): String {
        if (rounds == null) return "[]"
        val array = JSONArray()
        for (r in rounds) {
            val obj = JSONObject()
            obj.put("roundNumber", r.roundNumber)
            obj.put("teamName", r.teamName)
            obj.put("scoreChange", r.scoreChange)
            obj.put("timestamp", r.timestamp)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toRoundList(data: String?): List<Round> {
        if (data.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<Round>()
        try {
            val array = JSONArray(data)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Round(
                        roundNumber = obj.getInt("roundNumber"),
                        teamName = obj.getString("teamName"),
                        scoreChange = obj.getInt("scoreChange"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
