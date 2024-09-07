package com.example.statski

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.example.statski.databinding.ActivityAthleteStatsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.google.android.gms.fitness.data.DataPoint
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat


@RequiresApi(Build.VERSION_CODES.O)
class AthleteStats : AppCompatActivity() {
    lateinit var binding : ActivityAthleteStatsBinding
    private lateinit var currentAthlete: Athlete



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAthleteStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()



        val bundle : Bundle? = intent.extras
        val athlete_picked = bundle!!.getString("athlete_picked")
        val Gson = Gson()
        val current_athlete : Athlete = Gson.fromJson(athlete_picked, Athlete::class.java)





        if (current_athlete != null){
            // Filling name, nation and birth textviews
            binding.title.text = current_athlete.name
            binding.nation.text = current_athlete.nation
            binding.birth.text = current_athlete.birth.toString()

            // Filling victories, podiums and total points textviews
            // If an athlete has no victories or podiums, these data are replaced with top5 and top10
            val victories = current_athlete.CountVictories()
            if(victories == 0){
                val top5 = current_athlete.CountTop5()
                binding.victories.text = top5.toString()+" Top5"
            } else{
                binding.victories.text = victories.toString()+" victories"
            }

            val podiums = current_athlete.CountPodiums()
            if(podiums == 0){
                val top10 = current_athlete.CountTop10()
                binding.podiums.text = top10.toString()+" Top10"
            }
            else{
                binding.podiums.text = podiums.toString()+" podiums"
            }
            val career_races = current_athlete.performance_list.size
            binding.races.text = career_races.toString()+" races"


            val mostRecentPerformance = current_athlete.getMostRecentPerformanceDate()
            if (mostRecentPerformance != null){
                binding.lastRace.text = "Last race: "+mostRecentPerformance.toString()
            }
            // Setting linechart
            val last5 = current_athlete.GetLastFiveRaces(current_athlete.performance_list)

            last5.reversed()
            val entries = mutableListOf<Entry>()
            for((index, performance) in last5.withIndex()){

                performance.cup_points.toFloatOrNull()?.let{
                    entries.add(Entry(index.toFloat(), it))
                }
            }


        val backButton = binding.backButton
        backButton.setOnClickListener{
            finish()
        }


        // Spinner and Dataset changes
        val options = arrayOf("All race types", "Downhill", "Super G", "Giant Slalom", "Slalom", "Alpine Combined")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        val spinner = binding.selectCategory
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Setting default spinner element
        spinner.setSelection(0)
        // Handling spinner item to update dataset
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent : AdapterView<*>, view: View, position: Int, id: Long){
                val selectedCategory = options[position]
                val filteredPerformance : List<Performance> = when(selectedCategory){
                    "All race types" -> current_athlete.GetLastFiveRaces(current_athlete.performance_list)
                    else -> current_athlete.filterPerformanceByCategory(selectedCategory)
                }
                updateLineChart(filteredPerformance)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nothing to do
            }
        }
            // Seasonal stats
            val seasons = arrayOf("2023-24", "2022-23", "2021-22", "2020-21")//, "2019-20", "2018-19", "2017-18", "2016-17")
            val spin2_adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                seasons
            )
            val spinner2 = binding.statsSpinner
            spin2_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = spin2_adapter
            // spinner2.setSelection(0)
            spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, Position: Int, id: Long) {
                    val selectedSeason = seasons[Position]
                    var start: String = "October 01, 2023"
                    var end: String = "March 31, 2024"
                    when (selectedSeason) {
                        "2023-24" -> {
                            start = "October 01, 2023"
                            end = "March 31, 2024"
                        }

                        "2022-23" -> {
                            start = "October 01, 2022"
                            end = "March 31, 2023"
                        }

                        "2021-22" -> {
                            start = "October 01, 2021"
                            end = "March 31, 2022"
                        }

                        "2020-21" -> {
                            start = "October 01, 2020"
                            end = "March 31, 2021"
                        }

                        else -> {
                            Log.d("AthleteStats", "Please select a valid season")
                        }

                    }
                    if(start != null && end != null){
                        val seasonal_performances = current_athlete.filterPerformanceBySeason(start, end)
                        binding.racesNumber.text = seasonal_performances.size.toString()
                        binding.seasonWins.text = current_athlete.CountVictories(seasonal_performances).toString()
                        binding.seasonPodiums.text = current_athlete.CountPodiums(seasonal_performances).toString()
                        binding.avgPointsPerRace.text = current_athlete.Avg_points_per_race(seasonal_performances)
                        binding.winRate.text = current_athlete.CalculateWinRate(seasonal_performances)
                        binding.podiumRate.text = current_athlete.CalculatePodiumRate(seasonal_performances)

                    }




                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // nothing to do
                }
            }


        }

    }
    private fun updateLineChart(performanceList: List<Performance>) {


        // Ordina le performance per data e seleziona le ultime 5
        val sortedPerformanceList = performanceList.sortedByDescending { performance ->
            SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).parse(performance.date)
        }.take(5)

        val categoryMap = mapOf(
            "Women's Super G" to "SG", "Men's Super G" to "SG",
            "Men's Downhill" to "DH", "Women's Downhill" to "DH",
            "Women's Alpine combined" to "AC", "Men's Alpine combined" to "AC",
            "Men's Slalom" to "SL", "Women's Slalom" to "SL",
            "Men's Giant Slalom" to "GS", "Women's Giant Slalom" to "GS"
        )
        if (sortedPerformanceList.isEmpty()) {
            Toast.makeText(this, "No races available for the selected category", Toast.LENGTH_SHORT).show()
            return
        }

        // Creazione degli entry per il LineDataSet
        val entries = sortedPerformanceList.mapIndexed { index, performance ->
            val cupPoints = performance.cup_points.toFloatOrNull() ?: 0f
            Entry(index.toFloat(), cupPoints)

        }.filterNotNull()

        // Possibile motivo crash: viene invertito perchè non è mutableList
        // val reversed_entries = entries.reversed()

        // Creazione e configurazione del LineDataSet
        val dataSet = LineDataSet(entries, "Points scored in the last 5 races").apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = resources.getColor(R.color.blue, null)
            valueTextColor = resources.getColor(R.color.black, null)
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return (value).toInt().toString()
                }
            }
            setValueTextSize(14f)
        }

        // Aggiornamento del grafico con il nuovo dataset
        binding.linechart.data = LineData(dataSet)

        // Configurazione dell'asse X per visualizzare le categorie abbreviate
        val categories = sortedPerformanceList.map { it.category }
        val xAxisFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < categories.size) {
                    categoryMap[categories[index]] ?: ""
                } else {
                    ""
                }
            }
        }

        binding.linechart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = xAxisFormatter
            textSize = 14f

        }

        // Configurazione dell'asse Y
        binding.linechart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
            textSize = 14f
            axisMinimum = 0f
        }
        binding.linechart.axisRight.isEnabled = false
        binding.linechart.description.isEnabled = false
        // Invalida e ridisegna il grafico con i nuovi dati
        binding.linechart.invalidate()
    }
}