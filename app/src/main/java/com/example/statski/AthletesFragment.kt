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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.statski.databinding.FragmentAthletesBinding
import com.google.android.material.tabs.TabLayout.LabelVisibility
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.statski.databinding.ItemAthletesLayoutBinding

class AthletesFragment : Fragment() {

    // Create databinding
    private lateinit var binding: FragmentAthletesBinding
    val viewModel_instance : AthletesViewModel by viewModels()

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
        // Get Athlets list from ViewModel
        val athletesList = viewModel_instance.athletesMap.values.toList()
        Log.d("AthletesFragment", "Number of athletes: ${athletesList.size}")
        val adapter = AthleteAdapter(requireContext(), athletesList)
        binding.rvAthletesList.adapter = adapter

    }
}

class AthleteAdapter(val context: Context, val athleteList: List<Athlete>) :
    RecyclerView.Adapter<AthleteAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: ItemAthletesLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAthletesLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val athlete = athleteList[position]
        holder.binding.athlete = athlete
        holder.binding.executePendingBindings()
    }
    override fun getItemCount(): Int {
        return athleteList.size
    }
}


