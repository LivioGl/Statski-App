package com.example.statski

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.statski.databinding.FragmentAthletesBinding
import com.example.statski.databinding.FragmentWCSlopesBinding
import com.example.statski.databinding.SlopesListBinding
import java.util.Locale

val slopeImageMap = mapOf(
    "Gran Risa" to R.drawable.granrisa,
    "Chuenisbargli" to R.drawable.adelboden,
    "Rettenbach" to R.drawable.solden,
    "La Face de Bellevarde" to R.drawable.valdisere,
    "Streif" to R.drawable.streif,
    "Stelvio" to R.drawable.bormio,
    "Lauberhorn" to R.drawable.wengen,
    "Saslong" to R.drawable.saslong,
    "Birds of Prey" to R.drawable.beavercreek,
    "Olympiabakken" to R.drawable.kvitfjell,
    "Mont Lachaux" to R.drawable.cransmontana,
    "Olimpia delle Tofane" to R.drawable.cortina,
    "Corviglia" to R.drawable.stmoritz,
    "Planai" to R.drawable.schladming,
    "Levi Black" to R.drawable.levi,
    "3-Tre" to R.drawable.campiglio,
    "Ganslern" to R.drawable.ganslern,
    "Oreiller-Killy" to R.drawable.valdiseredownhill
)

class WCSlopesFragment : Fragment() {

    // Create databinding
    private lateinit var binding : FragmentWCSlopesBinding
    val viewModel_instance : SlopesViewModel by activityViewModels()
    private var SlopesList = mutableListOf<Slope>()
    private lateinit var slopesAdapter: SlopesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWCSlopesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel_instance
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("WCSlopesFragment", "Fragment view created")
        binding.rvSlopes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSlopes.setHasFixedSize(true)

        // Get Slopes from ViewModel
        SlopesList = viewModel_instance.SlopesMap.values.toMutableList()
        Log.d("SlopesFragment", "Number of Slopes: ${SlopesList.size}")
        slopesAdapter = SlopesAdapter(requireContext(), SlopesList)
        binding.rvSlopes.adapter = slopesAdapter

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
        if(query!=null){
            var lowercase_query = query.lowercase(Locale.getDefault())
            var filteredList = SlopesList.filter{it.name.lowercase(Locale.ROOT).contains(lowercase_query)}

            if(filteredList.isEmpty()){
                Toast.makeText(requireContext(), "No Slope found", Toast.LENGTH_SHORT).show()
            } else{
                slopesAdapter.setFilteredList(filteredList)
            }
        } else slopesAdapter.setFilteredList(SlopesList)
    }

}



class SlopesAdapter(val context: Context, var SlopesList : List<Slope>):
        RecyclerView.Adapter<SlopesAdapter.ViewHolder>(){
            inner class ViewHolder(val binding : SlopesListBinding): RecyclerView.ViewHolder(binding.root)

    fun setFilteredList(ListSlopes: List<Slope>){
        this.SlopesList = ListSlopes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SlopesListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slope = SlopesList[position]
        holder.binding.slopeId.text = slope.name
        // Get the correct image from map using slope.name
        val imgId = slopeImageMap[slope.name] ?: R.drawable.ic_launcher_background
        holder.binding.slopePic.setImageResource(imgId)
        holder.binding.slopeRace.text = slope.race_host
        holder.binding.slopeStart.text = slope.start_high
        holder.binding.slopeEnd.text = slope.end_high
        holder.binding.slopeMountain.text = slope.mountain
        holder.binding.slopeLocation.text = slope.location
        holder.binding.slopeVerticalDrop.text = slope.vertical_drop
        holder.binding.slopeNation.text = slope.nation
        holder.binding.raceSteep.text = slope.steep
        holder.binding.raceLength.text = slope.length
    }

    override fun getItemCount(): Int {
        return SlopesList.size
    }
}