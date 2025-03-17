;(ns cn.lambdalib2.cgui.loader
;  (:import [cn.lambdalib2.cgui Widget WidgetContainer]
;           [cn.lambdalib2.cgui.component Component Transform]
;           [cn.lambdalib2.s11n.xml DOMS11n]
;           [cn.lambdalib2.util Debug ResourceUtils]
;           [net.minecraft.util ResourceLocation]
;           [org.w3c.dom Document Element Node NodeList]
;           [org.xml.sax SAXException]
;           [javax.xml.parsers DocumentBuilder DocumentBuilderFactory]
;           [javax.xml.transform Transformer TransformerFactory DOMSource StreamResult]
;           [java.io File FileOutputStream InputStream OutputStream IOException]
;           [java.util ArrayList List Optional]))
;
;(def TAG_WIDGET "Widget")
;(def TAG_COMPONENT "Component")
;
;(def s11n (DOMS11n/instance))
;(def db (let [dbf (DocumentBuilderFactory/newInstance)]
;          (.setIgnoringElementContentWhitespace dbf true)
;          (.newDocumentBuilder dbf)))
;
;(defn to-std-list [^NodeList l]
;  (let [ret (ArrayList.)]
;    (dotimes [i (.getLength l)]
;      (.add ret (.item l i)))
;    ret))
;
;(defn read-internal [doc]
;  (let [ret (WidgetContainer.)
;        root (.getFirstChild doc)]
;    (if (or (nil? root) (not= "Root" (.getNodeName root)))
;      (throw (RuntimeException. "Root widget invalid")))
;    (doseq [n (filter #(= TAG_WIDGET (.getNodeName %))
;                (to-std-list (.getChildNodes root)))]
;      (read-widget ret ^Element n))
;    ret))
;
;(defn read-widget [container node]
;  (let [w (Widget.)
;        name (.getAttribute node "name")]
;    (doseq [n (to-std-list (.getChildNodes node))]
;      (case (.getNodeName n)
;        TAG_WIDGET (read-widget w ^Element n)
;        TAG_COMPONENT (let [comp (read-component ^Element n)]
;                        (when comp
;                          (if (= "Transform" (.name comp))
;                            (do (.removeComponent w Transform)
;                                (set! (.transform w) comp))
;                            (.addComponent w comp)))))))
;  (if (not (.addWidget container name w))
;    (Debug/warnFormat "Name clash while reading widget: %s, it is ignored." name))))
;
;(defn read-component [node]
;  (try
;    (let [klass (Class/forName (.getAttribute node "class"))]
;      (Optional/ofNullable (.deserialize s11n klass node)))
;    (catch Exception e
;      (do (Debug/error "Failed reading component" e)
;          Optional/empty))))
;
;(defn write-internal [container doc]
;  (let [root (.createElement doc "Root")]
;    (doseq [widget (.getDrawList container)]
;      (let [elem (.createElement doc TAG_WIDGET)]
;        (write-widget (.getName widget) widget elem)
;        (.appendChild root elem))))
;  (.appendChild doc root))
;
;(defn write-widget [name w dst]
;  (let [doc (.getOwnerDocument dst)]
;    (.setAttribute dst "name" name)
;    (doseq [c (.getComponentList w)]
;      (.appendChild dst (write-component c doc)))
;    (doseq [child (.getDrawList w)]
;      (let [elem (.createElement doc TAG_WIDGET)]
;        (write-widget (.getName child) child elem)
;        (.appendChild dst elem)))))
;
;
;(defn write-component [component doc]
;  (let [ret (.serialize s11n doc TAG_COMPONENT component)]
;    (.setAttribute ret "class" (.getCanonicalName (.getClass component)))
;    ret))
;
;(defn write-doc [dst doc]
;  (try
;    (let [transformer-factory (TransformerFactory/newInstance)
;          transformer (.newTransformer transformer-factory)
;          source (DOMSource. doc)
;          result (StreamResult. dst)]
;      (.transform transformer source result))
;    (catch Exception e
;      (Debug/error "Can't write CGUI document" e))))
;
;(defn read [^InputStream in]
;  (try
;    (read-internal (.parse db in))
;    (catch Exception e
;      (throw (RuntimeException. e)))))
;
;(defn read [location]
;  (read (ResourceUtils/getResourceStream location)))
;
;(defn write [container ^OutputStream out]
;  (let [doc (.newDocument db)]
;    (write-internal container doc)
;    (write-doc out doc)))
;
;(defn write [container dest]
;  (with-open [ofs (FileOutputStream. dest)]
;    (write container ofs)))
;;
;(def transformer (transformer-factory.newTransformer))
;(def source (DOMSource. doc))
;(def result (StreamResult. dst))
;(try
;  (.transform transformer source result)
;  (catch Exception e
;    (Debug/error "Can't write CGUI document" e)))
