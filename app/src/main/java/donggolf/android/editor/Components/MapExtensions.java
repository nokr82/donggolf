/*
 * Copyright (C) 2016 Muhammed Irshad
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package donggolf.android.editor.Components;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.view.View;
import android.widget.ImageView;

import donggolf.android.R;
import donggolf.android.editor.Components.ComponentsWrapper;
import donggolf.android.editor.Components.CustomEditText;
import donggolf.android.editor.EditorComponent;
import donggolf.android.editor.EditorCore;
import donggolf.android.editor.Utilities.Utilities;
import donggolf.android.editor.models.EditorContent;
import donggolf.android.editor.models.EditorControl;
import donggolf.android.editor.models.EditorType;
import donggolf.android.editor.models.Node;
import donggolf.android.editor.models.RenderType;
import org.jsoup.nodes.Element;

/**
 * Created by mkallingal on 5/1/2016.
 */
public class MapExtensions extends EditorComponent {
    EditorCore editorCore;
    private int mapExtensionTemplate= R.layout.tmpl_image_view;

    @Override
    public Node getContent(View view) {
        Node node = getNodeInstance(view);
        EditorControl mapTag = (EditorControl) view.getTag();
        Editable desc = ((donggolf.android.editor.Components.CustomEditText) view.findViewById(R.id.desc)).getText();
        node.content.add(mapTag.Cords);
        node.content.add(desc.length() > 0 ? desc.toString() : "");
        return node;
    }

    @Override
    public String getContentAsHTML(Node node, EditorContent content) {
      return componentsWrapper.getHtmlExtensions().getTemplateHtml(node.type).replace("{{$content}}",
              componentsWrapper.getMapExtensions().getCordsAsUri(node.content.get(0))).replace("{{$desc}}", node.content.get(1));
    }

    @Override
    public void renderEditorFromState(Node node, EditorContent content) {
        insertMap(node.content.get(0), node.content.get(1), true);
    }

    @Override
    public Node buildNodeFromHTML(Element element) {
        return null;
    }

    @Override
    public void init(ComponentsWrapper componentsWrapper) {
        this.componentsWrapper = componentsWrapper;
    }

    public MapExtensions(EditorCore editorCore){
        super(editorCore);
        this.editorCore = editorCore;
    }

    public void setMapViewTemplate(int drawable)
    {
        this.mapExtensionTemplate= drawable;
    }



    public String getMapStaticImgUri(String cords, int width){
        StringBuilder builder = new StringBuilder();
        builder.append("http://maps.google.com/maps/api/staticmap?");
        builder.append("size="+String.valueOf(width)+"x400&zoom=15&sensor=true&markers="+cords);
        return builder.toString();
    }

    public void insertMap(String cords, String desc, boolean insertEditText) {
//        String image="http://maps.googleapis.com/maps/api/staticmap?center=43.137022,13.067162&zoom=16&size=600x400&maptype=roadmap&sensor=true&markers=color:blue|43.137022,13.067162";
        String[] x= cords.split(",");
        String lat = x[0];
        String lng = x[1];
        int[]size= Utilities.getScreenDimension(editorCore.getContext());
        int width=size[0];
//        ImageView imageView = new ImageView(context);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400);
//        params.bottomMargin=12;
//        imageView.setLayoutParams(params);
//        parentView.addView(imageView);
//        Picasso.with(this.context).load(builder.toString()).into(imageView);

        final View childLayout = ((Activity) this.editorCore.getContext()).getLayoutInflater().inflate(this.mapExtensionTemplate, null);
        ImageView imageView = childLayout.findViewById(R.id.imageView);
        componentsWrapper.getImageExtensions().loadImageUsingLib(getMapStaticImgUri(String.valueOf(lat)+","+String.valueOf(lng),width), imageView);

        /**
         * description, if render mode, set the description and disable it
         */
        CustomEditText editText = childLayout.findViewById(R.id.desc);
        if(editorCore.getRenderType()== RenderType.Renderer){
            editText.setText(desc);
            editText.setEnabled(false);
        }
        /*
         *  remove button
         */

        final View btn =  childLayout.findViewById(R.id.btn_remove);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setVisibility(View.VISIBLE);
            }
        });
        imageView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorCore.getParentView().removeView(childLayout);
            }
        });
        EditorControl control = editorCore.createTag(EditorType.map);
        control.Cords= cords;
        childLayout.setTag(control);
        int Index= editorCore.determineIndex(EditorType.map);
        editorCore.getParentView().addView(childLayout, Index);
        if(insertEditText){
          componentsWrapper.getInputExtensions().insertEditText(Index + 1, null, null);
        }
    }

    public void loadMapActivity(){
               // Intent intent=new Intent(this.editorCore.getContext(), MapsActivity.class);
               // ((Activity) this.editorCore.getContext()).startActivityForResult(intent, 123);
    }

    public CharSequence getCordsAsUri(String s) {
        return getMapStaticImgUri(s,800);
    }
}
