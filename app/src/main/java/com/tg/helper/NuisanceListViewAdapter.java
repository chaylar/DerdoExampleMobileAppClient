package com.tg.helper;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.tg.VO.AppUserSettingsVO;
import com.tg.dataObject.FreshListDataObject;
import com.tg.dataObject.NuisanceType;
import com.tg.derdoapp.R;

import java.util.ArrayList;
import java.util.List;

public class NuisanceListViewAdapter extends ArrayAdapter<NuisanceType> {

    private final Activity relatedContext;
    private final List<NuisanceType> relatedList;

    private IAppUserSettingNuisanceSetter nuisanceSetter;
    private int selectedNuisanceCode;

    public NuisanceListViewAdapter(Activity context, List<NuisanceType> freshList, IAppUserSettingNuisanceSetter nuisanceSetter, int selectedNuisanceCode) {
        super(context, R.layout.helper_nuisance_select_list_item, freshList);

        this.relatedContext = context;
        this.relatedList = freshList;
        this.nuisanceSetter = nuisanceSetter;
        this.selectedNuisanceCode = selectedNuisanceCode;
        //this.allItemsList = new ArrayList<>();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final NuisanceType relatedObject = relatedList.get(position);

        LayoutInflater inflater = relatedContext.getLayoutInflater();
        final Button button = (Button)inflater.inflate(R.layout.helper_nuisance_select_list_item, null, true);
        button.setText(relatedObject.nuisanceName);
        button.setTag(relatedObject.typeCode);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuisanceSetter.setNuisance(relatedObject.typeCode, button);
            }
        });

        //allItemsList.add(button);
        nuisanceSetter.addToButtonList(button);

        if(selectedNuisanceCode == relatedObject.typeCode) {
            button.callOnClick();
        }

        return button;
    };

    /*public List<Button> getAllItemsList() {
        return allItemsList;
    }*/

    public interface IAppUserSettingNuisanceSetter {
        void setNuisance(int nuisanceTypeCode, Button b);
        void addToButtonList(Button b);
    }
}