import React, { useState, useEffect } from 'react';

function App() {
    const [date, setDate] = useState({
        "month" : (new Date().getMonth() + 1).toString(),
        "day" : (new Date().getDate()).toString()
    });
    const [description, setDescription] = useState(null);
    const [items, setItems] = useState(null);
    const [modalShow, setModalShow] = useState(false);
    const [modalContent, setModalContent] = useState(null);

    useEffect(
        () => {
            const url = "/api/description";

            fetch(url, {
                method : 'GET'
            })
            .then(response => response.json())
            .then(data => {
                setDescription(data);
            })
            .catch(error => console.log(error));
        },
        []
    );

    function handleDateChange(newDate) {
        const today = new Date();
        if (newDate.month === "")
            newDate.month = (today.getMonth() + 1).toString();
        if (newDate.day === "")
            newDate.day = today.getDay().toString();
        setDate(newDate);
    }

    function handleFormValidation(formData) {
        const url = "/api/items";

        fetch(url, {
            method : 'POST',
            body: JSON.stringify(formData)
        })
        .then(response => response.json())
        .then(data => {
            setItems(data);
        })
        .catch(error => console.log(error));
    }

    function handleItemPurchase(purchaseData) {
        const url = "/api/item/purchase";
        fetch(url, {
            method : 'POST',
            body: JSON.stringify(purchaseData)
        })
        .then(response => response.json())
        .then(data => {
            setModalContent(data);
            setModalShow(true);
        })
        .catch(error => console.log(error));
    }

    function handleModalClose() {
        setItems(null)
    }

    return (
        <div>
            <DateInput date={date} onDateChange={handleDateChange} />
            <SideBar
                date={date}
                description={description} 
                onFormValidation={handleFormValidation}
            />
            <Items 
                items={items} 
                onItemPurchase={handleItemPurchase}
            />
            <Modal
                show={modalShow}
                content={modalContent}
                onModalClose={handleModalClose}
            />
        </div>
    );
}

function DateInput(props) {
    function handleDateChange(e) {
        let newDate = {
            "month" : props.date.month,
            "day" : props.date.day
        }

        const input = e.target;
        newDate[input.name] = input.value;

        props.onDateChange(newDate);
    }

    return (
        <div>
            <div>
                <input id="month-input" type="text" name="month" 
                    placeholder={props.date.month} onChange={handleDateChange}/>
                <label for="month-input">Month</label>
            </div>
            <div>
                <input id="day-input" type="text" name="day" 
                    placeholder={props.date.day} onChange={handleDateChange} />
                <label for="day-input">Day</label>
            </div>
        </div>
    );
}

function SideBar(props) {
    const [category, setCategory] = useState("");

    const metaFeatures = ["id", "bundle", "month", "day"];

    function handleCategorySelection(e) {
        const select = e.target;
        const selectedCategory = select.options[select.selectedIndex].value;
        setCategory(selectedCategory);
    }

    function handleFormValidation(e) {
        e.preventDefault();

        const form = e.target;
        const inputs = Array.from(form.getElementsByTagName("input"));
        let data = {
            "category" : category,
            "filters" : [],
            "date" : props.date
        };

        inputs.forEach(input => {
            const feature = input.name;
            const value = input.value;
            const type = input.type
            if (value !== "" && type !== "submit") {
                data.filters.push({
                    "feature" : feature,
                    "condition" : "=",
                    "value" : value
                });
            }
        });

        props.onFormValidation(data);
    }

    if (props.description) {
        const categoriesOptions = props.description.map((entry) => {
            return (
                <option value={entry.category}>{entry.category}</option>
            );
        });


        let form = null;
        if (category !== "") {
            const features = groupBy(props.description, "category", "features")[category];
            const inputs = features.map(feature => {
                if (! metaFeatures.includes(feature)) {
                    const id = feature + "-input";
                    return (
                        <div>
                            <input id={id} type="text" name={feature} />
                            <label for={id}>{feature}</label>
                        </div>
                    );
                } else {
                    return null;
                }
            });
            form = (
                <form onSubmit={handleFormValidation}>
                    {inputs}
                    <div>
                        <input type="submit" value="Search" />
                    </div>
                </form>
            );
        }
        return (
            <div>
                <div>
                    <select onChange={handleCategorySelection}>
                        <option id="category-select" value="" selected={true}>-- Select a category --</option>
                        {categoriesOptions}
                    </select>
                    <label for="category-select">Categories</label>
                </div>
                <div>
                    {form}
                </div>
            </div>
        );
    }
}

function Items(props) {
    if (props.items) {

        const stores = props.items.sort((a, b) => a.seller > b.seller ? 1 : a.seller < b.seller ? -1 : 0).map(batch => {
            const seller = batch.seller;
            const promotions = batch.promotions;
            const items = batch.items;

            const promotionsStructured = promotions.map(promotion => {
                return (
                    <div>
                        <h3>{promotion.title} - {promotion.discount}%</h3>
                        <p>Starts on {promotion["start month"]}/{promotion["start day"]} and ends on {promotion["end month"]}/{promotion["end day"]}</p>
                    </div>
                );
            });
            const itemsStructured = items.map(item => {
                const itemDescription = Object.entries(item).map((key, value) => key + " : " + value).join(", ");
                return (
                    <div>
                        {itemDescription}
                    </div>
                );
            });
            return (
                <div>
                    <h1>{seller}</h1>
                    {promotionsStructured}
                    {itemsStructured}
                </div>
            );
        });

        return (
            <div>
                {stores}
            </div>
        );
    }
}

function Modal(props) {
}

function groupBy(list, key, value) {
    const map = {};
    list.forEach((item) => {
        const k = item[key];
        let v = null;
        if (Array.isArray(value)) {
            v = {};
            value.forEach(item => {
                v[item] = list[item];
            });
        } else {
            v = item[value];
        }
        map[k] = v;
    });
    return map;
}
export default App;
