package com.example.statski

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.statski.databinding.FragmentAthletesBinding
import com.google.android.material.tabs.TabLayout.LabelVisibility
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import org.json.JSONArray
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.statski.databinding.ItemAthletesLayoutBinding
import com.google.gson.Gson
import java.util.Locale

class AthletesFragment : Fragment() {

    // Create databinding
    private lateinit var binding: FragmentAthletesBinding
    val viewModel_instance : AthletesViewModel by activityViewModels()
    private var athletesList = mutableListOf<Athlete>()
    private lateinit var Athl_adapter: AthleteAdapter
    private var currentFilterText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAthletesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel_instance
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("AthletesFragment", "Fragment view created")
        binding.rvAthletesList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAthletesList.setHasFixedSize(true)

        // Get Athlets list from ViewModel
        athletesList = viewModel_instance.athletesMap.values.toMutableList()
        Log.d("AthletesFragment", "Number of athletes: ${athletesList.size}")
        // Adapter instance
        Athl_adapter = AthleteAdapter(requireContext(), athletesList)

        binding.rvAthletesList.adapter = Athl_adapter

        // SearchView Configuration
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilterText = newText
                filterList(newText)
                return true
            }
        })
         //[Thesis] Onclicklistener for radio buttons
        binding.sortSurname.setOnCheckedChangeListener {_, isChecked->
            SortAthletes()
        }

        binding.sortButtonsAge.setOnCheckedChangeListener { _, checkedId ->
            SortAthletes()
        }

        binding.deleteFilter.setOnClickListener{
            clearFilters()
        }




        // Override the item click method to open the new fragment
        Athl_adapter.setOnItemClickListener(object : AthleteAdapter.OnItemClickListener{
            override fun OnItemClick(athlete: Athlete){
                // Handling item click

                // Get the athlete which user chose
                viewModel_instance.selectAthlete(athlete)
                Log.d("AthletesFragment", "Athlete picked: ${athlete}")

                val athlete_picked = Gson().toJson(athlete)
                val intent = Intent(activity, AthleteStats::class.java)
                intent.putExtra("athlete_picked", athlete_picked)
                startActivity(intent)

            }
        })

    }
    // Filter field
    private fun filterList(query : String?){
        if(query!= null){
            var lowercase_query = query.lowercase(Locale.getDefault())
            var filteredList = athletesList.filter{it.name.lowercase(Locale.ROOT).contains(lowercase_query)}

            if(filteredList.isEmpty()){
                Toast.makeText(requireContext(), "No Athlete found", Toast.LENGTH_SHORT).show()
            } else{
                Athl_adapter.setFilteredList(filteredList)
            }
        } else Athl_adapter.setFilteredList(athletesList)
    }




    // [Thesis] Sorting athletes by name, year up or year down
    private fun SortAthletes(){

        val filteredList = if(currentFilterText.isNullOrEmpty()){
            athletesList
        } else{
            athletesList.filter{athlete->
                athlete.name.lowercase(Locale.getDefault()).contains(currentFilterText!!.lowercase(Locale.getDefault()))
            }
        }

        val sortByName = binding.sortSurname.isChecked
        val sortByYearAscending = binding.sortButtonsAge.checkedRadioButtonId == R.id.year_up
        val sortByYearDescending = binding.sortButtonsAge.checkedRadioButtonId == R.id.year_down

        val currentList = Athl_adapter.athleteList

        val sortedList = when{
            sortByName && sortByYearAscending -> {
                currentList.sortedWith(compareBy<Athlete> {it.birth}.thenBy { it.name })
            }
            sortByName && sortByYearDescending ->{
                currentList.sortedWith(compareByDescending<Athlete> {it.birth}.thenBy{it.name})
            }
            sortByYearAscending->{
                currentList.sortedBy { it.birth }
            }
            sortByYearDescending->{
                currentList.sortedByDescending { it.birth }
            }
            sortByName->{
                currentList.sortedBy { it.name }
            }

            else -> currentList
        }
        Athl_adapter.setFilteredList(sortedList)
    }

    // [Thesis] Remove filters added on AthleteList
    private fun clearFilters(){
        binding.sortSurname.isChecked = false
        binding.sortButtonsAge.clearCheck()

        filterList(currentFilterText)

    }

}


class AthleteAdapter(val context: Context, var athleteList: List<Athlete>) :
    RecyclerView.Adapter<AthleteAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: ItemAthletesLayoutBinding): RecyclerView.ViewHolder(binding.root)

    fun setFilteredList(ListAthletes : List<Athlete>){
        this.athleteList = ListAthletes
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAthletesLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val athlete = athleteList[position]
        holder.binding.athlId.text = athlete.name
        holder.binding.natId.text = athlete.nation
        holder.binding.year.text = athlete.birth.toString()
        holder.binding.executePendingBindings()

        // Item click manage
        holder.itemView.setOnClickListener{
            onItemClickListener?.OnItemClick(athlete)
        }
    }
    override fun getItemCount(): Int {
        return athleteList.size
    }

    // Interface for item click
    interface OnItemClickListener{
            fun OnItemClick(athlete: Athlete)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }

}


