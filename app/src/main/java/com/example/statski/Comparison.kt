package com.example.statski

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.example.statski.databinding.FragmentAthletesBinding
import com.example.statski.databinding.FragmentComparisonBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

    // [Thesis]

class Comparison : Fragment() {

    private lateinit var binding: FragmentComparisonBinding
    val viewModel_instance: AthletesViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val AthletesList: MutableList<Athlete> =
            viewModel_instance.athletesMap.values.toMutableList()
        val searchView1 = binding.searchAthlete1
        val searchView2 = binding.searchAthlete2
        val spinner1 = binding.spinnerAthlete1
        val spinner2 = binding.spinnerAthlete2

        val spinnerAdapter1 = SpinnerAdapter1(requireContext(), AthletesList.toMutableList())
        val spinnerAdapter2 = SpinnerAdapter2(requireContext(), AthletesList.toMutableList())

        spinnerAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner1.adapter = spinnerAdapter1
        spinner2.adapter = spinnerAdapter2

        searchView1.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrEmpty()) {
                    AthletesList
                } else {
                    AthletesList.filter { it.name.contains(newText, ignoreCase = true) }
                        .toMutableList()
                }
                spinnerAdapter1.clear()
                spinnerAdapter1.addAll(filteredList)
                spinnerAdapter1.notifyDataSetChanged()

                return true
            }
        })
        searchView2.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrEmpty()) {
                    AthletesList
                } else {
                    AthletesList.filter { it.name.contains(newText, ignoreCase = true) }
                        .toMutableList()
                }
                spinnerAdapter2.clear()
                spinnerAdapter2.addAll(filteredList)
                spinnerAdapter2.notifyDataSetChanged()

                return true
            }
        })

        binding.sendData.setOnClickListener {

            if (spinner1.selectedItem == null || spinner2.selectedItem == null) {
                Toast.makeText(requireContext(), "Please select two athletes", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val selectedAthlete1 = spinner1.selectedItem as Athlete
            val selectedAthlete2 = spinner2.selectedItem as Athlete

            if (selectedAthlete1.name == selectedAthlete2.name) {
                Toast.makeText(
                    requireContext(),
                    "You selected the same athlete!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val commonPerformances = SearchCommonPerformances(selectedAthlete1, selectedAthlete2)
            if (commonPerformances.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "There are no races in common between ${selectedAthlete1.name} and ${selectedAthlete2.name}.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            var last10Performances = commonPerformances
                .sortedByDescending { it.getDateAsLocalDate() }
                .take(10)

            last10Performances = last10Performances.reversed()
            val list1 = mutableListOf<Performance>()
            val list2 = mutableListOf<Performance>()
            val linechart = binding.linechartAthletes

            last10Performances.forEachIndexed { index, performance ->
                if (index % 2 == 0) {
                    list2.add(performance)
                } else {
                    list1.add(performance)
                }
            }

            setUpLineChart(linechart, list1, list2, selectedAthlete1, selectedAthlete2)
        }

    }


    class SpinnerAdapter1(context: Context, private val athletesList: MutableList<Athlete>) :
        ArrayAdapter<Athlete>(context, android.R.layout.simple_spinner_item, athletesList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            (view as TextView).text = athletesList[position].name
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            (view as TextView).text = athletesList[position].name
            return view
        }
    }

    class SpinnerAdapter2(context: Context, private val athletesList: MutableList<Athlete>) :
        ArrayAdapter<Athlete>(context, android.R.layout.simple_spinner_item, athletesList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            (view as TextView).text = athletesList[position].name
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            (view as TextView).text = athletesList[position].name
            return view
        }
    }

    private fun SearchCommonPerformances(athlete1: Athlete, athlete2: Athlete): List<Performance> {
        val commonPerformances = mutableListOf<Performance>()

        for (performance1 in athlete1.performance_list) {
            for (performance2 in athlete2.performance_list) {
                if (performance1.date == performance2.date &&
                    performance1.place == performance2.place &&
                    performance1.category == performance2.category
                ) {


                    commonPerformances.add(performance1)
                    commonPerformances.add(performance2)
                }
            }
        }
        return commonPerformances
    }

    private fun setUpLineChart(
        lineChart: LineChart,
        list1: List<Performance>,
        list2: List<Performance>,
        athl1: Athlete,
        athl2: Athlete
    ) {
        val entries1 = list1.mapIndexedNotNull { index, performance ->
            performance.cup_points.toFloatOrNull()?.let { Entry(index.toFloat(), it) }
        }
        val entries2 = list2.mapIndexedNotNull { index, performance ->
            performance.cup_points.toFloatOrNull()?.let { Entry(index.toFloat(), it) }
        }

        val dataSet1 = LineDataSet(entries1, athl1.name).apply {
            color = resources.getColor(R.color.blue)
            valueTextColor = resources.getColor(R.color.black)
            valueTextSize = 10f
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                private val format = DecimalFormat("0")
                override fun getFormattedValue(value: Float): String {
                    return format.format(value)
                }
            }


        }

        val dataSet2 = LineDataSet(entries2, athl2.name).apply {
            color = resources.getColor(R.color.black)
            valueTextColor = resources.getColor(R.color.black)
            valueTextSize = 10f
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                private val format = DecimalFormat("0")
                override fun getFormattedValue(value: Float): String {
                    return format.format(value)
                }
            }
        }

        val lineData = LineData(dataSet1, dataSet2)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setLabelCount(entries1.size.coerceAtLeast(entries2.size), true)
        xAxis.labelRotationAngle = 45f
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.textColor = resources.getColor(R.color.black)

        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                val performanceList = list1 + list2
                return if (index >= 0 && index < performanceList.size) {
                    performanceList[index].changeToCompactFormat()
                } else {
                    ""
                }
            }
        }


        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = resources.getColor(R.color.black)
        yAxisLeft.setLabelCount(5, true)
        yAxisLeft.axisMinimum = 0f

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        lineChart.setExtraOffsets(10f, 10f, 10f, 40f)

        lineChart.legend.apply {
            xOffset = 10f
            yOffset = 10f
            textSize = 12f
            formSize = 10f
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }

        lineChart.minimumWidth = dpToPx(350)
        lineChart.minimumHeight = dpToPx(220)

        lineChart.invalidate()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }


}




