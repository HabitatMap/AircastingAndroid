package io.lunarlogic.aircasting.lib

import com.opencsv.CSVWriter
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.StatefulBeanToCsv
import com.opencsv.bean.StatefulBeanToCsvBuilder
import io.lunarlogic.aircasting.models.Session
import java.io.FileWriter

class CsvHelper {

    companion object{

        var fileWriter: FileWriter? = null
        var csvWriter: CSVWriter? = null
        var beanToCsv: StatefulBeanToCsv<Session>? = null

        // todo: jakiś header dodać na górze tego csv-a
        // TODO- this is just a first version/ trial of working with csv, needs to be improved later on

        fun sessionToCsv(){
            fileWriter = FileWriter("something.csv")
            csvWriter = CSVWriter(fileWriter)

            val mappingStrategy = ColumnPositionMappingStrategy<Session>()
            mappingStrategy.type = Session::class.java
//            mappingStrategy.setColumnMapping()

            beanToCsv = StatefulBeanToCsvBuilder<Session>(fileWriter)
                .withMappingStrategy(mappingStrategy) //todo: possibly some more options to be added
                .build()

            fileWriter!!.flush()
            fileWriter!!.close()
            csvWriter!!.close()

        }
    }
}
