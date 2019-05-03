package donggolf.android.editor;

import android.view.View;

import donggolf.android.editor.Components.ComponentsWrapper;
import donggolf.android.editor.EditorCore;
import donggolf.android.editor.models.EditorContent;
import donggolf.android.editor.models.EditorControl;
import donggolf.android.editor.models.EditorType;
import donggolf.android.editor.models.Node;

import org.jsoup.nodes.Element;

import java.util.ArrayList;

public abstract class EditorComponent {
    private final EditorCore editorCore;
    protected ComponentsWrapper componentsWrapper;
    public abstract Node getContent(View view);
    public abstract String getContentAsHTML(Node node, EditorContent content);
    public abstract void renderEditorFromState(Node node, EditorContent content);
    public abstract Node buildNodeFromHTML(Element element);
    public abstract void init(ComponentsWrapper componentsWrapper);

    public EditorComponent(EditorCore editorCore){
        this.editorCore = editorCore;
    }

    protected Node getNodeInstance(View view){
        Node node = new Node();
        EditorType type = editorCore.getControlType(view);
        node.type = type;
        node.content = new ArrayList<>();
        return node;
    }
    protected Node getNodeInstance(EditorType type){
        Node node = new Node();
        node.type = type;
        node.content = new ArrayList<>();
        return node;
    }

}