package com.example.statski

import android.R
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.statski.databinding.ActivityAthleteStatsBinding
import com.example.statski.databinding.ActivityFocusOnFavAthleteBinding
import com.example.statski.databinding.ItemFocusOnFavAthleteLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

    // [Thesis]

class FocusOnFavAthlete : AppCompatActivity() {
    private lateinit var binding : ActivityFocusOnFavAthleteBinding
    private lateinit var Perf_adapter : PerformanceAdapter
    private var performanceList = mutableListOf<Performance>()
    private var currentFilterText : String? = null
    private var startSeason : String? = null
    private var endSeason : String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFocusOnFavAthleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backButton = binding.goBackToFavList
        backButton.setOnClickListener {
            finish()
        }

        val bundle : Bundle? = intent.extras
        val athlete_picked = bundle!!.getString("athlete_picked")
        val Gson = Gson()
        val current_athlete : Athlete = Gson.fromJson(athlete_picked, Athlete::class.java)
        Perf_adapter = PerformanceAdapter(this, current_athlete.performance_list)

        binding.rvPerformanceList.adapter = Perf_adapter
        binding.rvPerformanceList.layoutManager = LinearLayoutManager(this)
        performanceList = current_athlete.performance_list
        performanceList.map{
            if(it.position.toInt() > 30){
                it.cup_points = "0"
            }
        }

        val spinner_select_season = binding.filterBySeason
        val seasons = arrayOf("All seasons", "2023-24", "2022-23", "2021-22", "2020-21")
        val spinner_select_season_Adapter = ArrayAdapter(this, R.layout.simple_spinner_item, seasons)
        spinner_select_season_Adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner_select_season.adapter = spinner_select_season_Adapter
        spinner_select_season.setSelection(0)

        spinner_select_season.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (seasons[position]) {
                    "All seasons" ->{
                        startSeason = null
                        endSeason = null
                    }
                    "2023-24" -> {
                        startSeason = "October 01, 2023"
                        endSeason = "March 31, 2024"
                    }
                    "2022-23" -> {
                        startSeason = "October 01, 2022"
                        endSeason = "March 31, 2023"
                    }
                    "2021-22" -> {
                        startSeason = "October 01, 2021"
                        endSeason = "March 31, 2022"
                    }
                    "2020-21" -> {
                        startSeason = "October 01, 2020"
                        endSeason = "March 31, 2021"
                    }
                    else -> {
                        startSeason = null
                        endSeason = null
                    }
                }
                SortPerformances(startSeason, endSeason)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                SortPerformances(startSeason, endSeason)
            }
        }

        performanceList.sortByDescending { it.getDateAsLocalDate() }

        binding.sortRank.setOnCheckedChangeListener { _, isChecked ->
            val sortedList = if (isChecked) {

                performanceList
                    .filter { it.position.toIntOrNull() != null }
                    .sortedBy { it.position.toIntOrNull() }
            } else {
                performanceList
            }
            Perf_adapter.setFilteredList(sortedList)
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilterText = newText
                SortPerformances(startSeason, endSeason)
                return true
            }
        })
        binding.sortRank.setOnCheckedChangeListener { _, _ -> SortPerformances(startSeason, endSeason) }

        binding.deleteFilter.setOnClickListener{
            binding.sortRank.isChecked = false
            spinner_select_season.setSelection(0)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun SortPerformances(startSeason: String?, endSeason: String?) {
        var filteredList = performanceList.toMutableList()



        if (startSeason != null && endSeason != null) {
            val startDate = LocalDate.parse(startSeason, DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH))
            val endDate = LocalDate.parse(endSeason, DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH))
            filteredList = filteredList.filter { performance ->
                val performanceDate = performance.getDateAsLocalDate()
                performanceDate.isAfter(startDate.minusDays(1)) && performanceDate.isBefore(endDate.plusDays(1))
            }.toMutableList()
        }


        filteredList = when {
            binding.sortRank.isChecked && !currentFilterText.isNullOrEmpty() && startSeason != null -> {
                filteredList.filter { it.position.toIntOrNull() != null }
                    .sortedBy { it.position.toIntOrNull() }
                    .toMutableList()
            }
            binding.sortRank.isChecked && startSeason != null -> {
                filteredList.filter { it.position.toIntOrNull() != null }
                    .sortedBy { it.position.toIntOrNull() }
                    .toMutableList()
            }
            binding.sortRank.isChecked && !currentFilterText.isNullOrEmpty() -> {
                filteredList.filter { it.position.toIntOrNull() != null }
                    .sortedBy { it.position.toIntOrNull() }
                    .toMutableList()
            }
            binding.sortRank.isChecked -> {
                filteredList.filter { it.position.toIntOrNull() != null }
                    .sortedBy { it.position.toIntOrNull() }
                    .toMutableList()
            }
            !currentFilterText.isNullOrEmpty() && startSeason != null -> {
                filteredList.sortedByDescending { it.getDateAsLocalDate() }
                    .toMutableList()
            }
            startSeason != null -> {
                filteredList.sortedByDescending { it.getDateAsLocalDate() }
                    .toMutableList()
            }
            !currentFilterText.isNullOrEmpty() -> {
                filteredList.sortedByDescending { it.getDateAsLocalDate() }
                    .toMutableList()
            }
            else -> {
                filteredList.sortedByDescending { it.getDateAsLocalDate() }
                    .toMutableList()
            }
        }
        if (!currentFilterText.isNullOrEmpty()) {
            filteredList = filteredList.filter { performance ->
                performance.place.lowercase(Locale.getDefault())
                    .contains(currentFilterText!!.lowercase(Locale.getDefault()))
            }.toMutableList()
        }
        Perf_adapter.setFilteredList(filteredList)
    }





    private fun filterPerformanceList(query: String?){
        if(query != null){
            var lowercase_query = query.lowercase(Locale.getDefault())
            var filteredList = performanceList.filter{it.place.lowercase(Locale.ROOT).contains(lowercase_query)}

            if(filteredList.isEmpty()){
                Toast.makeText(this, "No races found in location you typed", Toast.LENGTH_SHORT).show()
            } else{
                Perf_adapter.setFilteredList(filteredList)
            }
        } else{
            Perf_adapter.setFilteredList(performanceList)
        }

    }



}

class PerformanceAdapter(val context: Context, var performanceList : List<Performance>):
    RecyclerView.Adapter<PerformanceAdapter.ViewHolder>(){
        inner class ViewHolder(val binding : ItemFocusOnFavAthleteLayoutBinding): RecyclerView.ViewHolder(binding.root)


    fun setFilteredList(ListPerformances : List<Performance>){
        this.performanceList = ListPerformances
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFocusOnFavAthleteLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var performance = performanceList[position]
        holder.binding.performancePosition.text = performance.position
        holder.binding.perforanceId.text = performance.place+" "+performance.category
        holder.binding.dateId.text = performance.date
        holder.binding.timeId.text = performance.total_time
        holder.binding.pointsId.text = performance.cup_points
    }

    override fun getItemCount(): Int {
        return performanceList.size
    }

}
