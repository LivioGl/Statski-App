package com.example.statski

import android.content.Context
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.statski.databinding.ItemAthletesLayoutBinding
import java.util.Locale

class AthletesFragment : Fragment() {

    // Create databinding
    private lateinit var binding: FragmentAthletesBinding
    val viewModel_instance : AthletesViewModel by activityViewModels()
    private var athletesList = mutableListOf<Athlete>()
    private lateinit var Athl_adapter: AthleteAdapter

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
        Athl_adapter = AthleteAdapter(requireContext(), athletesList)
        binding.rvAthletesList.adapter = Athl_adapter

        // SearchView Configuration
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
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
    }
    override fun getItemCount(): Int {
        return athleteList.size
    }
}


