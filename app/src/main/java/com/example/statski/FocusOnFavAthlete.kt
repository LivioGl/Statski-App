package com.example.statski

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
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

class FocusOnFavAthlete : AppCompatActivity() {
    private lateinit var binding : ActivityFocusOnFavAthleteBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var Perf_adapter : PerformanceAdapter

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





    }
}

class PerformanceAdapter(val context: Context, var performanceList : List<Performance>):
    RecyclerView.Adapter<PerformanceAdapter.ViewHolder>(){
        inner class ViewHolder(val binding : ItemFocusOnFavAthleteLayoutBinding): RecyclerView.ViewHolder(binding.root)

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
